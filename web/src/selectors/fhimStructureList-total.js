export default (fhimStructureList) => {
  return fhimStructureList
      .map((fhimStructure) => fhimStructure.resourceUsage)
      .reduce((sum, value) => sum + value, 0);
};
