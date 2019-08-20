import React from 'react';
import { connect } from 'react-redux';
import { Input} from '@progress/kendo-react-inputs';
import { GridColumn as Column, Grid} from '@progress/kendo-react-grid';
import { Button } from '@progress/kendo-react-buttons';
import uuid from 'uuid';

import {StructureLoader} from '../data/StructureLoader.jsx';
import {FHIMStructureEditorForm }      from './FHIMStructureEditorForm.jsx';
import {ColumnNameHeader} from './renderers.jsx';
import {structurePageCount} from '../data/properties.jsx';


export class FHIMStructurePage extends React.Component {

    offset = { left: 500, top: 50 };
    constructor(props) {
        super(props);
        this.state = {
            resources: { data: [], total: 0, transactionId:''},
            dataState: { take: structurePageCount, skip: 0 },
            queryDefinition: {searchBy: '', transactionId:'', newSearch: false},
            searchOn: false,
            structureEntryInEdit: undefined,
            structureEntrys: undefined,
            rowCount:0,
            show: 1,
            infoMsg: 'INFO Message',
            errorMsg: '',
            sort: [
                { field: 'name', dir: 'asc' }
            ]
        };
    }
       
    render() {
        return (
            <div className="content-container">
              
              <div align="right" className="k-form">
                 {this.searchInput()}
             </div>
               {this.searchResults()}                          
            </div>
            
        );
    }

    searchInput=() => (
       
        <div>
           
            <Input
                   style={{ width: '260px', height: '50px', valign: "bottom", backgroundColor:"white"} }   
                   label="Structure Name"              
                    minLength={1}
                    defaultValue =''
                    required={false}         
                    name="serchFilter"
                    onChange={this.onSearchChanged}>
             </Input>
    
                &nbsp;&nbsp;
           
             <Button icon="search" onClick={this.onSearchClick} 
                     style={{ height: '32px', valign: "bottom", weight: "bold" } }
                     primary={true}>Search
             </Button>
           
        
        </div>
    );
    
    searchResults=() => (
       
        <div>
               
        <Grid       
                    filterable={false}
                    pageable={true}
                    resizable={true}
                    {...this.state.dataState}
                    {...this.state.resources}
                    onDataStateChange={this.dataStateChange}
                    style={{backgroundColor:"rgb(227, 231, 237)"}}
                    onRowClick={(e) => {
                        this.setState({ structureEntryInEdit: this.cloneStructure(e.dataItem) })
                    }} 
                    sortable
                    sort={this.state.sort}
                    onSortChange={(e) => {
                        let fn  = this.state.sort[0].field;
                        if(e.sort[0] && e.sort[0].field)
                        {
                            fn = e.sort[0].field;
                            const dir = e.sort[0].dir;
                            if(fn.length > 0)
                            {
                                 this.setState({
                                     sort: e.sort
                                });
                            }
                        }
                        else if(this.state.sort[0].dir === 'desc')
                        {
                           
                            this.setState({
                                sort: [
                                      { field:fn, dir: 'asc' }
                                    ]
                                });
                        }
                        else
                        {
                           
                            this.setState({
                                sort: [
                                      { field:fn, dir: 'desc' }
                                    ]
                                });
                        }
                       
                      }}
                    >
                    
                    <Column field="resource.name" filter="text" title="Structure Name"    
                           headerCell={ColumnNameHeader} cell={StructureNameCell}/>
                    <Column field="resource.type" filter="text" title="Type" 
                         headerCell={ColumnNameHeader}  cell={StructureTypeCell}/>

                </Grid>
                <div>
                {
                    (this.state.searchOn) ? 
                    ( <StructureLoader
                          dataState={this.state.dataState}
                          onDataRecieved={this.dataRecieved} 
                          queryDefinition={this.state.queryDefinition}  
                          sort={this.state.sort} />
                    )  :  (<p></p>)
                }
                {this.clearSearchState()}
                </div>

                <br /><br />
              
                {this.state.structureEntryInEdit &&
                    
                   <FHIMStructureEditorForm 
                      dataItem={this.state.structureEntryInEdit} save={this.save} cancel={this.cancel} 
                   />
                }
                   
        </div>
    );
    
    dataStateChange = (e) => {
      
        this.setState({
            ...this.state,
            dataState: e.data
        });

    }

    dataRecieved = (resources) => {

        const searchBy = this.state.queryDefinition.searchBy;         
       
        this.setState({
            ...this.state,
            resources: resources,
            queryDefinition: {searchBy:searchBy, transactionId:resources.transactionId,newSearch:false }
        });

    }

    pageChange(event) {

        
        this.setState({
            data: this.state.data,
            skip: event.page.skip
        });
    }

    onRowFocusChange = (e) => {
       console.log("Focus Changed: "+e);
    };

    onTextChange = (e) => {
        this.props.setTextFilter(e.target.value);
    };


    onSearchChanged = (e) => {

        const searchBy = e.target.value;
        const transactionId = this.state.queryDefinition.transactionId;

      //  console.log("SET-> SEARCH BY-> "+searchBy);

        this.setState({
      
        queryDefinition: {searchBy:searchBy, transactionId:transactionId,newSearch:false }
        });
      
    };

    onSearchClick = (e) => {
        e.preventDefault();
        
        const searchBy = this.state.queryDefinition.searchBy;   
        this.setState({
           queryDefinition: {searchBy:searchBy, transactionId:'',newSearch:true }
        });
        this.setState({searchOn: true});
        this.setState({dataState: { take: structurePageCount, skip: 0 }});
    };

    clearSearchState = () => {

       this.setState.newSearch = false;
    };

    clearStatusMsg = () => {
        this.state.infoMsg = '';
        this.state.errorMsg = '';
    };


    edit = (dataItem) => {
        this.setState({ structureEntryInEdit: this.cloneStructure(dataItem) });
    }

    remove = (dataItem) => {


        const structureEntrys = this.state.resources.data.slice();

        const index = structureEntrys.findIndex(p => p.resource.id === dataItem.resource.id);

        if (index < 0) {
            console.log("Remove Structure: " + dataItem.resource.id + " Not Found");
            return;
        }
        // Udate server
        console.log("Removing Structure Definition entry record: " + dataItem.resource.id);

        structureEntrys.splice(index, 1);

        this.setState({
            structureEntrys: structureEntrys,
            structureEntryInEdit: undefined
        });
        
    }

    refreshPage = () =>{ 
        window.location.reload(); 
    }

     save = () => {
      
        const structureEntrys = this.state.resources.data;
        const searchBy = this.state.queryDefinition.searchBy;   
        const transactionId = this.state.queryDefinition.transactionId;   
      
        this.setState({
            structureEntrys: structureEntrys,
            dataItem: this.state.structureEntryInEdit,
            structureEntryInEdit: undefined,
            dataFeatchError:false,
            dataState: { take: structurePageCount, skip: 0 },   
            data: this.state.data,
            searchOn: true
        });

        console.log("Search BY BY: "+searchBy)
        this.setState({
      
            queryDefinition: {searchBy:searchBy, transactionId:transactionId,newSearch:false }
            });
    }

    cancel = () => {
        this.setState({ structureEntryInEdit: undefined });
    }

    insert = () => {
        this.setState({ structureEntryInEdit: {} });
    }


    cloneStructure(structureEntryRecord) {

        const structureEntrys = this.state.resources.data;
        let structureEntryRef =
            structureEntrys.find(p => p.resource.id === structureEntryRecord.resource.id);


        if (!structureEntryRef) {
            console.log("PROFILE: " + structureEntryRef + " Not Found.");

        }
        return Object.assign({}, structureEntryRecord);
    }

    newTemplate(source) {

        const id = uuid.v4();
        const structureEntry = {
            resource: { id: id, name: '', url: '' }
        };


        console.log("NEW Structure: " + structureEntry);

        return Object.assign(structureEntry, source);
    }
       
}

class StructureNameCell extends React.Component {
    
    render() {
        let msg = "Click on the Row in order to load Structure Definition Entry.";
        return (
            <td  
                
                style={{color:"rgb(4, 66, 165)"}}
                title={msg}>
                {this.props.dataItem.resource.name}
            </td>
        );
    }
}

class StructureTypeCell extends React.Component {
    
    render() {
        let msg = "Click on the Row in order to load Structure Definition entry.";
        return (
            <td 
                style={{color:"rgb(4, 66, 165)"}}
                title={msg} >
                {this.props.dataItem.resource.type} 
            </td>
        );
    }
}

const mapStateToProps = (state, props) => ({
   
  });
  
const mapDispatchToProps = (dispatch, props) => ({
   
  });
  
 export default connect(mapStateToProps, mapDispatchToProps)(FHIMStructurePage);
  