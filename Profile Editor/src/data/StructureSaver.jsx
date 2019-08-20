import { baseURL,generateFunctionId } from './properties';
import { infoMessage, errorMessage } from '../actions/notifications';

export const updateTemplateStructure = (dataItem) => {
    const id = dataItem.resource.id;
    const endpoint = baseURL + id + "?_format=json";
    const jsonObj = JSON.stringify(dataItem.resource);

    showObject("Update",endpoint, jsonObj);

    fetch(endpoint, {
        method: 'PUT',
        headers: {
            'Accept-Charset': 'utf-8',
            'Accept': 'application/json',
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: jsonObj
    })
        .then((response) => {
            console.log("Update Operation Successful.");
        })
        .catch((error) => {
            errorMessage("Failed to Update Structure: " + error + ", URL: " + endpoint);
        });
}

export const createTemplateStructure = (dataItem) => {
    const endpoint = baseURL + "?_format=json";
    const jsonObj = JSON.stringify(dataItem.resource);

    showObject("Insert", endpoint, jsonObj);
    fetch(endpoint, {
        method: 'POST',
        headers: {
            'Accept-Charset': 'utf-8',
            'Accept': 'application/json',
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: jsonObj
    })
        .then((response) => {
            console.log("Create Operation Successful.");
        })
        .catch((error) => {
            errorMessage("Failed to Generate Structure: " + error + ", URL: " + endpoint);

        });
};

export const generateProfile = (dataItem) => {
    const id = dataItem.resource.id;
    const endpoint = baseURL + generateFunctionId +  id;
        
    console.log("Generate Profile " + endpoint);
    let win = window.open(endpoint, '_blank');
    win.focus();
}

export const showObject = (id, endpoint, item) => {
    console.log("Id: "+id+ " Structure Request URL:   " + endpoint);
    console.log("************ BEGIN JSON **********");
    console.log(item);
    console.log("********** END JSON ************");
};
