import * as firebase from 'firebase';

// Web app's Firebase configuration
var firebaseConfig = {
    apiKey: "AIzaSyAyw3mwDprDZbrawMOpJuOk6R6dg2lFszE",
    authDomain: "fhim-2de9c.firebaseapp.com",
    databaseURL: "https://fhim-2de9c.firebaseio.com",
    projectId: "fhim-2de9c",
    storageBucket: "fhim-2de9c.appspot.com",
    messagingSenderId: "628713125497",
    appId: "1:628713125497:web:9a2686aa4ceaeaf6"
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

const database = firebase.database();
const googleAuthProvider = new firebase.auth.GoogleAuthProvider();

export { firebase, googleAuthProvider, database as default };

