package com.example.goukm.ui.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Data class representing a chat room between a customer and driver hwat
 */
data class ChatRoom(
    val id: String = "",
    val bookingId: String = "",
    val customerId: String = "",
    val driverId: String = "",
    val customerName: String = "",
    val driverName: String = "",
    val customerPhone: String = "",
    val driverPhone: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val customerUnreadCount: Int = 0,
    val driverUnreadCount: Int = 0,
    val isActive: Boolean = true
)

/**
 * Data class representing a chat message
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "", // "customer" or "driver"
    val text: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false
)

/**
 * Repository for managing chat functionality with Firebase Firestore
 */
object ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRoomsCollection = firestore.collection("chatRooms")

    /**
     * Creates a new chat room when a ride is accepted
     */
    suspend fun createChatRoom(
        bookingId: String,
        customerId: String,
        driverId: String,
        customerName: String,
        driverName: String,
        customerPhone: String,
        driverPhone: String
    ): Result<String> {
        return try {
            // Check if chat room already exists for this booking
            val existing = chatRoomsCollection
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            if (!existing.isEmpty) {
                return Result.success(existing.documents.first().id)
            }

            val chatRoomId = chatRoomsCollection.document().id
            val chatRoom = ChatRoom(
                id = chatRoomId,
                bookingId = bookingId,
                customerId = customerId,
                driverId = driverId,
                customerName = customerName,
                driverName = driverName,
                customerPhone = customerPhone,
                driverPhone = driverPhone,
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                customerUnreadCount = 0,
                driverUnreadCount = 0,
                isActive = true
            )

            chatRoomsCollection.document(chatRoomId).set(chatRoom).await()
            Result.success(chatRoomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a message in a chat room
     */
    suspend fun sendMessage(chatRoomId: String, text: String, senderName: String, senderRole: String): Result<Unit> {
        val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))

        return try {
            val messageId = chatRoomsCollection.document(chatRoomId)
                .collection("messages").document().id

            val message = Message(
                id = messageId,
                senderId = currentUserId,
                senderName = senderName,
                senderRole = senderRole,
                text = text,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Add message to subcollection
            chatRoomsCollection.document(chatRoomId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .await()

            // Get chat room to determine who to increment unread count for
            val chatRoomDoc = chatRoomsCollection.document(chatRoomId).get().await()
            val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)

            if (chatRoom != null) {
                // Update last message and increment unread count for the other party
                val updates = mutableMapOf<String, Any>(
                    "lastMessage" to text,
                    "lastMessageTime" to System.currentTimeMillis()
                )

                if (currentUserId == chatRoom.customerId) {
                    // Customer sent message, increment driver's unread count
                    updates["driverUnreadCount"] = chatRoom.driverUnreadCount + 1
                } else {
                    // Driver sent message, increment customer's unread count
                    updates["customerUnreadCount"] = chatRoom.customerUnreadCount + 1
                }

                chatRoomsCollection.document(chatRoomId).update(updates).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listens to messages in a chat room in real-time
     */
    fun listenToMessages(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        val listener = chatRoomsCollection.document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Marks all unread messages as read for the current user
     */
    suspend fun markMessagesAsRead(chatRoomId: String, isCustomer: Boolean): Result<Unit> {
        return try {
            val chatRoomDoc = chatRoomsCollection.document(chatRoomId).get().await()
            val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)
                ?: return Result.failure(Exception("Chat room not found"))

            // Reset unread count for current user role
            val update = if (isCustomer) {
                mapOf("customerUnreadCount" to 0)
            } else {
                mapOf("driverUnreadCount" to 0)
            }

            chatRoomsCollection.document(chatRoomId).update(update).await()

            // Mark all messages from the OTHER role as read
            val otherRole = if (isCustomer) "driver" else "customer"
            
            val messages = chatRoomsCollection.document(chatRoomId)
                .collection("messages")
                .whereEqualTo("senderRole", otherRole)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            if (!messages.isEmpty) {
                val batch = firestore.batch()
                for (doc in messages.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all chat rooms for the current user (as customer or driver)
     */
    fun getChatRoomsForUser(): Flow<List<ChatRoom>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // We need to listen to both customer and driver chats
        var customerListener: ListenerRegistration? = null
        var driverListener: ListenerRegistration? = null
        val customerChats = mutableListOf<ChatRoom>()
        val driverChats = mutableListOf<ChatRoom>()

        customerListener = chatRoomsCollection
            .whereEqualTo("customerId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                customerChats.clear()
                snapshot?.documents?.mapNotNull { it.toObject(ChatRoom::class.java) }?.let {
                    customerChats.addAll(it)
                }
                trySend((customerChats + driverChats).sortedByDescending { it.lastMessageTime })
            }

        driverListener = chatRoomsCollection
            .whereEqualTo("driverId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                driverChats.clear()
                snapshot?.documents?.mapNotNull { it.toObject(ChatRoom::class.java) }?.let {
                    driverChats.addAll(it)
                }
                trySend((customerChats + driverChats).sortedByDescending { it.lastMessageTime })
            }

        awaitClose {
            customerListener?.remove()
            driverListener?.remove()
        }
    }

    /**
     * Gets a chat room by booking ID
     */
    suspend fun getChatRoomByBookingId(bookingId: String): Result<ChatRoom?> {
        return try {
            val snapshot = chatRoomsCollection
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Result.success(null)
            } else {
                Result.success(snapshot.documents.first().toObject(ChatRoom::class.java))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets a chat room by its ID
     */
    suspend fun getChatRoom(chatRoomId: String): Result<ChatRoom?> {
        return try {
            val doc = chatRoomsCollection.document(chatRoomId).get().await()
            Result.success(doc.toObject(ChatRoom::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deactivates a chat room (when ride is completed or cancelled)
     */
    suspend fun deactivateChatRoom(chatRoomId: String): Result<Unit> {
        return try {
            chatRoomsCollection.document(chatRoomId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
