import moment from 'moment';

// Get visible fhimStructureList

export default (fhimStructureList, { text, sortBy, startDate, endDate }) => {
  return fhimStructureList.filter((fhimStructure) => {
    const createdAtMoment = moment(fhimStructure.createdAt);
    const startDateMatch = startDate ? startDate.isSameOrBefore(createdAtMoment, 'day') : true;
    const endDateMatch = endDate ? endDate.isSameOrAfter(createdAtMoment, 'day') : true;
    const textMatch = (text != '') && (
      (text === '*')||(fhimStructure.patientID.toLowerCase().includes(text.toLowerCase())));
    //console.log('Text Match Input: '+text+ ' Text Match ' + textMatch);
    return startDateMatch && endDateMatch && textMatch;
  }).sort((a, b) => {
    if (sortBy === 'date') {
      return a.createdAt < b.createdAt ? 1 : -1;
    } else if (sortBy === 'resourceUsage') {
      return a.resourceUsage < b.resourceUsage ? 1 : -1;
    }
  });
};
