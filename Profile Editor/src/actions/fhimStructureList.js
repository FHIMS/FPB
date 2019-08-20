import uuid from 'uuid';
import database from '../firebase/firebase';
import moment from 'moment';

// ADD_PROFILE_ENTRY
export const addFHIMStructure = (fhimStructure) => ({
  type: 'ADD_PROFILE_ENTRY',
  fhimStructure
});

export const startAddFHIMStructure = (fhimStructureData = {}) => {
  return (dispatch, getState) => {
    const uid = getState().auth.uid;
    const now = new Date().getTime();
  
    const {
      patientID = '',
      resourceID = '',
      resourceUsage = '',
      createdAt = new Date().getTime()
    } = fhimStructureData;
    const fhimStructure = { patientID, resourceID, resourceUsage,createdAt};

    return database.ref(`users/${uid}/fhimStructureList`).push(fhimStructure).then((ref) => {
      dispatch(addFHIMStructure({
        id: ref.key,
        ...fhimStructure
      }));
    });
  };
};

// REMOVE_PROFILE_ENTRY
export const removeFHIMStructure = ({ id } = {}) => ({
  type: 'REMOVE_PROFILE_ENTRY',
  id
});

export const startRemoveFHIMStructure = ({ id } = {}) => {
  return (dispatch, getState) => {
    const uid = getState().auth.uid;
    return database.ref(`users/${uid}/fhimStructureList/${id}`).remove().then(() => {
      dispatch(removeFHIMStructure({ id }));
    });
  };
};

// EDIT_PROFILE_ENTRY
export const editFHIMStructure = (id, updates) => ({
  type: 'EDIT_PROFILE_ENTRY',
  id,
  updates
});

export const startEditFHIMStructure = (id, updates) => {
  return (dispatch, getState) => {
    const uid = getState().auth.uid;
    return database.ref(`users/${uid}/fhimStructureList/${id}`).update(updates).then(() => {
      dispatch(editFHIMStructure(id, updates));
    });
  };
};

// SET_PROFILE_ENTRIES
export const setFHIMStructureList = (fhimStructureList) => ({
  type: 'SET_PROFILE_ENTRIES',
  fhimStructureList
});

export const initFHIMStructureList = () => {
  return (dispatch, getState) => {
    const uid = getState().auth.uid;
    return database.ref(`users/${uid}/fhimStructureList`).once('value').then((snapshot) => {
      const fhimStructureList = [];
/*
      snapshot.forEach((childSnapshot) => {
        fhimStructureList.push({
          id: childSnapshot.key,
          ...childSnapshot.val()
        });
      });
*/
      dispatch(setFHIMStructureList(fhimStructureList));
    });
  };
};


export const loadFHIMStructureList = () => {
  return (dispatch, getState) => {
    const uid = getState().auth.uid;
    return database.ref(`users/${uid}/fhimStructureList`).once('value').then((snapshot) => {
      const fhimStructureList = [];
      snapshot.forEach((childSnapshot) => {
        fhimStructureList.push({
          id: childSnapshot.key,
          ...childSnapshot.val()
        });
      });
      dispatch(setFHIMStructureList(fhimStructureList));
    });
  };
};
