const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.processSignUp = functions.auth.user().onCreate(async (user) => {
    const customClaims = {
        admin: user.email === 'admin@buksu.edu.ph'
    };
    
    try {
        await admin.auth().setCustomUserClaims(user.uid, customClaims);
        await admin.firestore().collection('metadata').doc(user.uid).set({
            refreshTime: admin.firestore.FieldValue.serverTimestamp(),
        });
    } catch (error) {
        console.log(error);
    }
});
