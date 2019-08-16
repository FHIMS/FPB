import React from 'react';
import ReactDOM from 'react-dom';

import { connect } from 'react-redux';
import { DropDownList } from '@progress/kendo-react-dropdowns';
import { GridColumn as Column, Grid, GridToolbar } from '@progress/kendo-react-grid';
import { Button } from '@progress/kendo-react-buttons';
import { Popup } from '@progress/kendo-react-popup';
import { Input } from '@progress/kendo-react-inputs';


import { TableNameHeader, ColumnNameHeader, Renderers } from './renderers.jsx';
import { updateTemplateStructure, createTemplateStructure, generateProfile } from '../data/StructureSaver.jsx';
import { warnNotification } from '../actions/notifications';
import {elementPageCount} from '../data/properties.jsx';

let availableElements = [];


/*
    ClassName.MyOrg.MyImplementationGuide.MyTemplate.ProfileVersion

*/

const classNameIx = 0, orgIx = 1, implementationGuideIx = 2, templateTitleIx = 3, templateVersionIx = 4;

export class FHIMStructureEditorForm extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            data: [],
            editItem: undefined,
            changes: false,
            resourceInEdit: this.props.dataItem || null,
            show: true,
            organizationName: '',
            implementationGuide: '',
            templateTitle: '',
            templateVersion: '',
            value: '',
            skip: 0,    
            take: elementPageCount,
            total: 0
        };
        this.pageChange = this.pageChange.bind(this);
        this.saveChanges = this.saveChanges.bind(this);
        this.cancelChanges = this.cancelChanges.bind(this);
        this.itemChange = this.itemChange.bind(this);
        this.renderers = new Renderers(this.enterEdit.bind(this), this.exitEdit.bind(this), 'inEdit');
        this.onChange = this.onChange.bind(this);
        this.onOrganizationNameChange = this.onOrganizationNameChange.bind(this);
        this.onImplementationGuideChange = this.onImplementationGuideChange.bind(this);
        this.onTemplateTitleChange = this.onTemplateTitleChange.bind(this);
        this.onTemplateVersionChange = this.onTemplateVersionChange.bind(this);
    }


    render() {
        const structureEntry = [this.state.resourceInEdit];
        let structureName = "Structure";
        if (this.isObjectClassType(structureEntry[0])) {
            structureName = "Class";
        }
        else if (this.isObjectTemplateType(structureEntry[0])) {
            structureName = "Template";
        }
        const tableHeader = structureName + ": " + structureEntry[0].resource.name;
        {this.initWidget(structureEntry)}
        return (

            <div className="content-container">

                <Popup className="popup-content" anchor={this.anchor}
                    show={this.state.show}
                    onClose={this.props.cancel}
                    position="top center"
                    align={{
                        horizontal: "center",
                        vertical: "center"
                    }}
                    popupClass={'popup-content'} >
                  
                    <div className="k-form" align="center">
                  
                        <Grid
                            style={{ backgroundColor: "rgb(227, 231, 237)", width: '950px' }}
                            data={availableElements.slice(this.state.skip, this.state.take + this.state.skip)}
                            pageSize={this.state.take}
                            skip={this.state.skip}
                            take={this.state.take}
                            total={this.state.total}
                            pageable={true}
                            onPageChange={this.pageChange}
                            resizable={true} 
                            onItemChange={this.itemChange}
                            editField="inEdit" >

                            <Column headerCell={ColumnNameHeader} title="Data Element" field="id" editable={false} />
                            <Column headerCell={ColumnNameHeader} title="Type" field="type" editable={true} cell={TypeCell} />
                            <Column headerCell={ColumnNameHeader} title="Usage" field="extensions" editable={true} cell={UsageDownCell} />

                            <GridToolbar>

                                <div align="left"
                                    style={{ color: "black", fontWeight: "bold", fontSize: "14px" }}>
                                    {tableHeader}
                                </div>
                            </GridToolbar>

                        </Grid>
                      
                        <br />
                        <div align="left">
                            {this.structureDefinitionInputs(structureEntry[0])}
                        </div>
                        <div align="right">
                            {this.structureDefinitionActions()}
                        </div>
                    
                    </div>

                </Popup>
            </div>
        );
    }

    generateFHIRProfile = (e) => {
        e.preventDefault();
        const structureEntry = this.state.resourceInEdit
        // console.log("Executing Generate structureEntry");
        generateProfile(structureEntry);
    }

    updateTemplate = (e) => {

        e.preventDefault();
        const errorMessage =
            "A new template requires an implementation guide,  a responsible organization, a template name, and a template version.";
        let structureEntry = this.state.resourceInEdit;
        let res = structureEntry.resource;

        structureEntry.resource.name = this.buildTemplateName(res);

        if (this.isObjectClassType(structureEntry)) {
            // Create template first          
            structureEntry = this.createTemplate(structureEntry);
        }
        else if (this.isObjectTemplateType(structureEntry)) {
            // Update
            // console.log("Update Tempate  Name: " + structureEntry.resource.name);
            updateTemplateStructure(structureEntry);
        }

        this.props.save();
    };


    createTemplate = (structureEntry) => {

        console.log("Before: " + structureEntry.resource.type);
        let clone = cloneStructure(structureEntry);
        let res = clone.resource;
        clone.resource.id = '';
        clone.resource.type = clone.resource.type.replace('class', 'template');
        clone.resource.version = this.state.templateVersion;
        clone.resource.title = this.state.templateTitle;
        clone.resource.name =   this.buildTemplateName(res);
        console.log("New Tempate  Name: " + clone.resource.name);

        structureEntry = createTemplateStructure(clone);

        return clone;

    };


    structureDefinitionInputs = (structureEntry) => (

        <div>

            <Input
                className="input-field"
                label="Organization Name"
                minLength={1}
                defaultValue={structureEntry.resource.publisher}
                required={true}
                name="organizationName"
                disabled={!this.enableInputRestrictFields()}
                onChange={this.onOrganizationNameChange}>
            </Input>
            <br /><br />
            <Input
                className="input-field"
                label="Implementation Guide"
                minLength={1}
                defaultValue={structureEntry.resource.implicitRules}
                required={true}
                name="implementationGuide"
                disabled={!this.enableInputRestrictFields()}
                onChange={this.onImplementationGuideChange}>
            </Input>
            <br /><br />
            <Input
                className="input-field"
                style={{ width: "55%" }}
                label="Profile Name"
                minLength={1}
                defaultValue={this.getTemplateTitle(structureEntry)}
                required={true}
                name="templateTitle"
                disabled={!this.enableInputRestrictFields()}
                onChange={this.onTemplateTitleChange}>
            </Input>
            <br /><br />
            <Input
                className="input-field"
                label="Profile Version"
                minLength={1}
                defaultValue={this.getTemplateVersion(structureEntry)}
                required={true}
                name="templateVersion"
                disabled={!this.enableInputFields()}
                onChange={this.onTemplateVersionChange}>
            </Input>
        </div>

    );

    structureDefinitionActions = () => (
        <div>

            <Button name="updateStructureDefinitionButton"
                disabled={!this.enableSaveStructure()}
                onClick={this.updateTemplate} className="k-button k-primary mt-1 mb-1">Save</Button>
            &nbsp;&nbsp;
           <Button name="genStructureButton"
                disabled={!this.enableGenerateStructure()}
                onClick={this.generateFHIRProfile}
                className="k-button k-primary mt-1 mb-1">Generate FHIR Profile</Button>
            &nbsp;&nbsp;
            <Button name="cancelButton" onClick={this.props.cancel} className="k-button k-primary mt-1 mb-1">Cancel</Button>

        </div>
    );
    
    getTemplateTitle = (entry) =>
    {
        if(entry.resource.title)
        {
            return entry.resource.title;
        }
        console.log("Structure: "+entry.resource.title+ " Title is missing.")
        return "";
    }

    getTemplateVersion = (entry) =>
    {
        if(entry.resource.version)
        {
            return entry.resource.version;
        }
        console.log("Structure: "+entry.resource.name+ " Version is missing.")
        return "";
    }
   
    buildTemplateName = (res) => {
        let className = this.getElementName(res.name, classNameIx);

        const newName =
            className +
            "." + res.publisher +
            "." + res.implicitRules +
            "." + res.title +
            "." + res.version;

        console.log("Build TEMPLATE NAME ("+newName+"): " +
            "  className-> " + className +
            "  res.publisher-> " + res.publisher +
            "  res.implicitRules-> " + res.implicitRules +
            "  res.title-> " + res.title +
            "  res.version-> " + res.version)
        return newName;

    }


    getElementName = (name, ix) => {
        const res = name.split('.');
        let element = '';

        if (res.length > ix) {
            element = res[ix];
        }
      //  console.log("Get Element: " + name + " ix: " + ix + " Result: " + element);
        return element;
    }


    initWidget = (structureEntry) => {

        availableElements = [];
        
        this.state.total = 0;
        if (structureEntry[0] && structureEntry[0].resource.snapshot && structureEntry[0].resource.snapshot) {
            this.state.data = structureEntry[0].resource.snapshot.element;
            const extension = this.state.data[0].extension;
            const len = this.state.data.length;

            if (extension && extension[0] && extension[0].valueString !== '') {

                availableElements = this.state.data.slice();
            }
            else {
                availableElements = this.state.data.slice(1, len);
            }
            
            this.state.total = availableElements.length;
            /*
             console.log("INIT Widget Element Length: " + availableElements.length +
            "  From " + this.state.data.length + " Scroll Window Size: " + this.state.take
                + " Skip: " + this.state.skip + " Total: "+ this.state.total);
            */
            
        }
        else {

            warnNotification("Missing Data Elements From the Structure: ");
        }
    };

    isObjectTemplateType = (structureEntry) => {
        // resource type
        let resourceType = structureEntry.resource.type;
        if (!resourceType) {
            // console.log("Warning, Structure Name is not defined");
            return false;
        }
        if (resourceType.endsWith('template')) {
            return true;
        }
        return false;
    }

    isObjectClassType = (structureEntry) => {
        // resource type
        let resourceType = structureEntry.resource.type;
        if (!resourceType) {
            // console.log("Warning, Structure Name is not defined");
            return false;
        }
        if (resourceType.endsWith('class')) {
            return true;
        }
        return false;
    }

    enableGenerateStructure = () => {

        if (this.isObjectTemplateType(this.state.resourceInEdit)&&this.enableSaveStructure())
        {
                return true;
        }
        return false;
    }

    enableSaveStructure = () => {

        return this.validateSaveStructure(false);
    }


    enableInputFields = () => {

        if (this.isObjectTemplateType(this.state.resourceInEdit) ||
            this.isObjectClassType(this.state.resourceInEdit)) {
            return true;
        }
        return false;
    }

    enableInputRestrictFields = () => {

        return this.enableInputFields();
    }

    validateSaveStructure = (showError) => {

        const structureEntry = this.state.resourceInEdit;

        let errList = '';
        let beginMessage = "Field: "
        let endMessage = " is required. "

        if (!structureEntry.resource.publisher || structureEntry.resource.publisher === '') {
            errList += "'Organization Name'";
        }
        if (!structureEntry.resource.implicitRules || structureEntry.resource.implicitRules === '') {
            if (errList !== '') {
                beginMessage = "Fields: "
                endMessage = " are required."
                errList += ", ";
            }
            errList += "'Implementation Guide'";
        }
        if (!structureEntry.resource.title || structureEntry.resource.title === '') {
            if (errList !== '') {
                beginMessage = "Fields: "
                endMessage = " are required."
                errList += ", ";
            }
            errList += "'Template Title'";
        }

        if (!structureEntry.resource.version || structureEntry.resource.version === '') {
            if (errList !== '') {
                beginMessage = "Fields: "
                endMessage = " are required."
                errList += ", ";
            }
            errList += "'Template Version'";
        }
       
        if (errList !== '') {
            const err = beginMessage + errList + endMessage;
            if (showError) {

                alert(err);
            }
            else {
                console.log(err);
            }

            return false;
        }

        return true;

    };

    onChange(e) {
        this.setState({ value: e.target.value });
    };

    onOrganizationNameChange = (e) => {
        const organizationName = e.target.value;
        this.setState({ organizationName: organizationName });

        let structureEntry = this.state.resourceInEdit;
        structureEntry.resource.publisher = organizationName;

    };

    onImplementationGuideChange = (e) => {
        const implementationGuide = e.target.value;
        this.setState({ implementationGuide: implementationGuide });
        let structureEntry = this.state.resourceInEdit;
        structureEntry.resource.implicitRules = implementationGuide;

    };

    onTemplateTitleChange = (e) => {
        const templateTitle = e.target.value;
        this.setState({ templateTitle: templateTitle });
        let structureEntry = this.state.resourceInEdit;
        structureEntry.resource.title = templateTitle;

    };

    onTemplateVersionChange = (e) => {
        const templateVersion = e.target.value;
        this.setState({ templateVersion: templateVersion });
        let structureEntry = this.state.resourceInEdit;
        structureEntry.resource.version = templateVersion;
    };


    rowRender = (trElement, props) => {
        const dataItem = props.dataItem;
        const resources = this.state.data.slice();
        const index = resources.findIndex(p => p.id === dataItem.id);
        const evenRow = { backgroundColor: "rgb(237, 242, 247)" };
        const oddRow = { backgroundColor: "rgb(rgb(252, 253, 255))" };
        const trProps = { style: index % 2 ? evenRow : oddRow };
        return React.cloneElement(trElement, { ...trProps }, trElement.props.children);
    };

    enterEdit(dataItem, field) {
        if (dataItem.inEdit && field === this.state.editField) {
            return;
        }
        this.exitEdit();
        dataItem.inEdit = field;
        this.setState({
            editField: field,
            data: this.state.data
        });
    }

    exitEdit() {
        this.state.data.forEach((d) => { d.inEdit = undefined; });
        this.setState({
            data: this.state.data,
            editField: undefined
        });
    }

    saveChanges(e) {

        e.preventDefault();
        this.setState({
            editField: undefined,
            changes: false
        });
    }

    cancelChanges(e) {
        e.preventDefault();
        this.setState({
            changes: false
        });
    }

    pageChange = (event) => {
      
    /*   
         console.log("Page Change Event: Skip: " + event.page.skip +
        " Take: "+event.page.take);
   */
      this.setState({
            skip: event.page.skip,
            take: event.page.take
        });
    }
    
    itemChange(event) {
        console.log("Item Change Event");
        event.dataItem[event.field] = event.value;
        this.setState({
            changes: true
        });
    }

}

class TypeCell extends React.Component {

    handleChange = (e) => {
        this.props.onChange({
            dataItem: this.props.dataItem,
            field: this.props.field,
            syntheticEvent: e.syntheticEvent,
            value: e.target.value.value
        });
    }

    render() {

        let typeValue = "Not Set";


        if (this.props.dataItem.type) {
            typeValue = this.props.dataItem.type[0].code;
            return (
                <td>
                    {typeValue}
                </td>

            );
        }
        else {
            console.log("TYPE NOT DEFINED");
        }

        // console.log("Type Code: " + typeValue + " ");


    }
}

class UsageDownCell extends React.Component {

    localizedData = [
        { text: "Supported", value: "supported" },
        { text: 'Mandatory', value: "mandatory" },
        { text: "Not supported", value: "not supported" }
    ];


    handleChange = (e) => {

        let extension = this.props.dataItem.extension;


        if (extension && extension[0] && extension[0].valueString) {

            /*   console.log("Handle Usage Cell"); // console.log("--->Handle Change: ''" +
                 this.props.field + "'" + " ''" +
                 extension[0].valueString + "'  New Vaue: ''" + e.target.value.value + "'");
             */

            extension[0].valueString = e.target.value.value;
        }
        else {
            /*
            console.log("Handle Usage Cell"); // console.log("--->Handle Change: ''" +
            this.props.field + " " + this.props.dataItem);
            */
        }

        this.props.onChange({
            dataItem: this.props.dataItem,
            field: this.props.field,
            syntheticEvent: e.syntheticEvent,
            value: e.target.value.value
        });
    }

    render() {

        let extension = this.props.dataItem.extension;

        let extensionValue = "not supported";
        if (extension) {
            extensionValue = extension[0].valueString;
        }
        else {
            return (
                <td>
                </td>

            );
        }

        return (
            <td>
                <DropDownList
                    style={{ width: "140px", fontSize: '12pt' }}
                    onChange={this.handleChange}
                    data={this.localizedData}
                    defaultValue={this.localizedData.find(c => c.value === extensionValue)}
                    textField="text"
                />
            </td>

        );
    }
}


function cloneStructure(structureEntry) {
    return Object.assign({}, structureEntry);
}

const mapStateToProps = (state, props) => ({

});

const mapDispatchToProps = (dispatch, props) => ({
    updateTemplateStructure: (structureEntry) => dispatch(updateTemplateStructure(structureEntry)),
    createTemplateStructure: (structureEntry) => dispatch(createTemplateStructure(structureEntry))
});

export default connect(mapStateToProps, mapDispatchToProps)(FHIMStructureEditorForm);

