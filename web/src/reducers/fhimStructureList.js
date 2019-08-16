// FHIMStructureContainer Reducer

const fhimStructureListReducerDefaultState = [];

export default (state = fhimStructureListReducerDefaultState, action) => {
  switch (action.type) {
    case 'ADD_PROFILE_ENTRY':
      return [
        ...state,
        action.fhimStructure
      ];
    case 'REMOVE_PROFILE_ENTRY':
      return state.filter(({ id }) => id !== action.id);
    case 'EDIT_PROFILE_ENTRY':
      return state.map((fhimStructure) => {
        if (fhimStructure.id === action.id) {
          return {
            ...fhimStructure,
            ...action.updates
          };
        } else {
          return fhimStructure;
        };
      });
    case 'SET_PROFILE_ENTRIES':
      return action.fhimStructureList;
    default:
      return state;
  }
};
