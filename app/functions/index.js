const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const {initializeApp} = require("firebase-admin/app");

initializeApp();

exports.sendChatNotification =
onDocumentCreated("notifications/{notificationId}",
    async (event) => {
      const notification = event.data.data();

      const message = {
        token: notification.token,
        notification: {
          title: notification.title,
          body: notification.body,
        },
        data: {
          chatId: notification.chatId,
          senderId: notification.senderId,
          type: notification.type,
        },
      };

      try {
        await getMessaging().send(message);
        // Delete the notification document after sending
        await event.data.ref.delete();
      } catch (error) {
        console.error("Error sending notification:", error);
      }
    });
