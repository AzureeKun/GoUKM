const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// 1. Notify Drivers when a new Ride Request is created
exports.sendNewRideRequestNotification = functions.firestore
    .document("bookings/{bookingId}")
    .onCreate(async (snapshot, context) => {
        const booking = snapshot.data();
        const pickup = booking.pickup;
        const dropOff = booking.dropOff;

        console.log(`New Ride Request: ${context.params.bookingId} from ${pickup} to ${dropOff}`);

        // Query all drivers who are available
        // Note: In large scale, use Geohashing or Topic Messaging
        const driversSnapshot = await admin.firestore()
            .collection("users")
            .where("role_driver", "==", true)
            .where("isAvailable", "==", true)
            .get();

        if (driversSnapshot.empty) {
            console.log("No available drivers found");
            return null;
        }

        const tokens = [];
        driversSnapshot.forEach(doc => {
            const data = doc.data();
            if (data.fcmToken) {
                tokens.push(data.fcmToken);
            }
        });

        if (tokens.length === 0) {
            console.log("No driver tokens found");
            return null;
        }

        const payload = {
            data: {
                type: "new_ride_request",
                bookingId: context.params.bookingId,
                pickup: pickup,
                dropOff: dropOff
            },
            notification: {
                title: "New Ride Request",
                body: `Trip from ${pickup}`
            }
        };

        // Send to all tokens
        // sendToDevice is deprecated but still works broadly; sendEachForMulticast is newer
        // For simplicity with legacy payloads: use sendToDevice or messaging().send()
        const response = await admin.messaging().sendToDevice(tokens, payload);
        console.log("Notifications sent:", response.successCount);
        return null;
    });

// 2. Notify Customer when Driver offers a fare
exports.sendNewOfferNotification = functions.firestore
    .document("bookings/{bookingId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();

        // Check if offeredFare has changed
        // We only notify if farewell is SET (not cleared) and different from before
        if (newData.offeredFare && newData.offeredFare !== oldData.offeredFare) {
            const userId = newData.userId;
            const driverId = newData.driverId;
            const fare = newData.offeredFare;

            console.log(`New Offer: Booking ${context.params.bookingId}, Fare ${fare}`);

            // Get Customer Token
            const userDoc = await admin.firestore().collection("users").document(userId).get();
            if (!userDoc.exists) return null;
            
            const token = userDoc.data().fcmToken;

            // Get Driver Name
            let driverName = "A Driver";
            if (driverId) {
                const driverDoc = await admin.firestore().collection("users").document(driverId).get();
                if (driverDoc.exists) {
                     driverName = driverDoc.data().name || "A Driver";
                }
            }

            if (!token) {
                 console.log("No customer token found");
                 return null;
            }

            const payload = {
                data: {
                    type: "new_offer",
                    bookingId: context.params.bookingId,
                    driverName: driverName,
                    fare: fare
                },
                notification: {
                    title: "New Fare Offer",
                    body: `${driverName} offered RM ${fare}`
                }
            };

            return admin.messaging().sendToDevice(token, payload);
        }
        return null;
    });

// 3. Notify Driver when Customer accepts the offer
exports.sendOfferAcceptedNotification = functions.firestore
    .document("bookings/{bookingId}")
    .onUpdate(async (change, context) => {
        const newData = change.after.data();
        const oldData = change.before.data();

        // Check if status changed to ACCEPTED
        if (newData.status === "ACCEPTED" && oldData.status !== "ACCEPTED") {
            const driverId = newData.driverId;
            const userId = newData.userId; 

            console.log(`Offer Accepted: Booking ${context.params.bookingId}`);

            if (!driverId) return null;

            // Get Driver Token
            const driverDoc = await admin.firestore().collection("users").document(driverId).get();
            if (!driverDoc.exists) return null;
            
            const token = driverDoc.data().fcmToken;
            
            // Get Customer Name
             const userDoc = await admin.firestore().collection("users").document(userId).get();
             const customerName = userDoc.data().name || "Customer";

            if (!token) return null;

            const payload = {
                data: {
                    type: "offer_accepted",
                    bookingId: context.params.bookingId,
                    customerName: customerName
                },
                notification: {
                    title: "Offer Accepted!",
                    body: `${customerName} accepted your ride.`
                }
            };

            return admin.messaging().sendToDevice(token, payload);
        }
        return null;
    });
