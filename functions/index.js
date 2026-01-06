const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");
const { getMessaging } = require("firebase-admin/messaging");

admin.initializeApp();
setGlobalOptions({ region: "asia-southeast1" });

// 1. Notify Drivers when a new Ride Request is created
exports.sendNewRideRequestNotification = onDocumentCreated("bookings/{bookingId}", async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const booking = snapshot.data();
    const pickup = booking.pickup;
    const dropOff = booking.dropOff;

    console.log(`New Ride Request: ${event.params.bookingId} from ${pickup} to ${dropOff}`);

    // Query all drivers who are available
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
            bookingId: event.params.bookingId,
            pickup: pickup,
            dropOff: dropOff
        }
    };

    // Send to all tokens
    const response = await getMessaging().sendEachForMulticast({
        tokens: tokens,
        data: payload.data
    });
    console.log("Notifications sent:", response.successCount);
    return null;
});

// 2. Notify Customer when Driver offers a fare
exports.sendNewOfferNotification = onDocumentUpdated("bookings/{bookingId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();

    // Check if offeredFare has changed
    if (newData.offeredFare && newData.offeredFare !== oldData.offeredFare) {
        const userId = newData.userId;
        const driverId = newData.driverId;
        const fare = newData.offeredFare;

        console.log(`New Offer: Booking ${event.params.bookingId}, Fare ${fare}`);

        // Get Customer Token
        const userDoc = await admin.firestore().collection("users").doc(userId).get();
        if (!userDoc.exists) return null;

        const token = userDoc.data().fcmToken;

        // Get Driver Name
        let driverName = "A Driver";
        if (driverId) {
            const driverDoc = await admin.firestore().collection("users").doc(driverId).get();
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
                bookingId: event.params.bookingId,
                driverName: driverName,
                fare: fare
            },
            token: token
        };

        return getMessaging().send(payload);
    }
    return null;
});

// 3. Notify Driver when Customer accepts the offer
exports.sendOfferAcceptedNotification = onDocumentUpdated("bookings/{bookingId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();

    // Check if status changed to ACCEPTED
    if (newData.status === "ACCEPTED" && oldData.status !== "ACCEPTED") {
        const driverId = newData.driverId;
        const userId = newData.userId;

        console.log(`Offer Accepted: Booking ${event.params.bookingId}`);

        if (!driverId) return null;

        // Get Driver Token
        const driverDoc = await admin.firestore().collection("users").doc(driverId).get();
        if (!driverDoc.exists) return null;

        const token = driverDoc.data().fcmToken;

        // Get Customer Name
        const userDoc = await admin.firestore().collection("users").doc(userId).get();
        if (!userDoc.exists) return null;
        const customerName = userDoc.data().name || "Customer";

        if (!token) return null;

        const payload = {
            data: {
                type: "offer_accepted",
                bookingId: event.params.bookingId,
                customerName: customerName
            },
            token: token
        };

        return getMessaging().send(payload);
    }
    return null;
});

// 4. Handle Driver Application Approval
exports.onDriverApplicationApproved = onDocumentUpdated("driverApplications/{userId}", async (event) => {
    const newData = event.data.after.data();
    const oldData = event.data.before.data();

    // 4.1 Handle Approval
    if (newData.status === "approved" && oldData.status !== "approved") {
        const userId = event.params.userId;
        console.log(`Driver Application Approved for user: ${userId}`);

        try {
            // Upgrade user role in Firestore
            await admin.firestore().collection("users").doc(userId).update({
                role_driver: true,
                licenseNumber: newData.licenseNumber || "",
                vehiclePlateNumber: newData.vehiclePlateNumber || "",
                vehicleType: "Car",
                carBrand: newData.carBrand || "",
                carColor: newData.carColor || "",
                bankQrUrl: newData.documents?.bank_qr || ""
            });

            // Send Approval Notification
            const userDoc = await admin.firestore().collection("users").doc(userId).get();
            if (userDoc.exists && userDoc.data().fcmToken) {
                const token = userDoc.data().fcmToken;
                await getMessaging().send({
                    token: token,
                    notification: {
                        title: "Welcome to the Team, Driver! 🚀",
                        body: "Congratulations! Your driver application has been approved. You can now switch to Driver Mode in your profile and start earning."
                    },
                    data: {
                        type: "application_approved"
                    }
                });
            }
            console.log(`User ${userId} successfully upgraded and notified.`);
        } catch (error) {
            console.error(`Error processing approval for ${userId}:`, error);
        }
    }

    // 4.2 Handle Rejection
    if (newData.status === "rejected" && oldData.status !== "rejected") {
        const userId = event.params.userId;
        const reason = newData.rejectionReason || "Please ensure your documents are clear and valid.";

        try {
            const userDoc = await admin.firestore().collection("users").doc(userId).get();
            if (userDoc.exists && userDoc.data().fcmToken) {
                const token = userDoc.data().fcmToken;
                await getMessaging().send({
                    token: token,
                    notification: {
                        title: "Driver Application Update 📋",
                        body: `We've reviewed your application and unfortunately, we couldn't approve it at this time. Reason: ${reason}`
                    },
                    data: {
                        type: "application_rejected",
                        reason: reason
                    }
                });
            }
        } catch (error) {
            console.error(`Error sending rejection notification for ${userId}:`, error);
        }
    }
});
