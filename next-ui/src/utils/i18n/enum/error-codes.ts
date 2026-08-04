import { defineMessage, type MessageDescriptor } from 'vue-intl'

export const errorCodeMessages: Record<string, MessageDescriptor> = {
  ERR_1000: defineMessage({
    description: 'Error code: ERR_1000',
    defaultMessage: 'File could not be accessed during analysis',
    id: 'app.error.ERR_1000',
  }),
  ERR_1001: defineMessage({
    description: 'Error code: ERR_1001',
    defaultMessage: 'Media type is not supported',
    id: 'app.error.ERR_1001',
  }),
  ERR_1002: defineMessage({
    description: 'Error code: ERR_1002',
    defaultMessage: 'Encrypted RAR archives are not supported',
    id: 'app.error.ERR_1002',
  }),
  ERR_1003: defineMessage({
    description: 'Error code: ERR_1003',
    defaultMessage: 'Solid RAR archives are not supported',
    id: 'app.error.ERR_1003',
  }),
  ERR_1004: defineMessage({
    description: 'Error code: ERR_1004',
    defaultMessage: 'Multi-Volume RAR archives are not supported',
    id: 'app.error.ERR_1004',
  }),
  ERR_1005: defineMessage({
    description: 'Error code: ERR_1005',
    defaultMessage: 'Unknown error while analyzing book',
    id: 'app.error.ERR_1005',
  }),
  ERR_1006: defineMessage({
    description: 'Error code: ERR_1006',
    defaultMessage: 'Book does not contain any page',
    id: 'app.error.ERR_1006',
  }),
  ERR_1007: defineMessage({
    description: 'Error code: ERR_1007',
    defaultMessage: 'Some entries could not be analyzed',
    id: 'app.error.ERR_1007',
  }),
  ERR_1008: defineMessage({
    description: 'Error code: ERR_1008',
    defaultMessage: "Unknown error while getting book's entries",
    id: 'app.error.ERR_1008',
  }),
  ERR_1009: defineMessage({
    description: 'Error code: ERR_1009',
    defaultMessage: 'A read list with that name already exists',
    id: 'app.error.ERR_1009',
  }),
  ERR_1015: defineMessage({
    description: 'Error code: ERR_1015',
    defaultMessage: 'Error while deserializing ComicRack CBL',
    id: 'app.error.ERR_1015',
  }),
  ERR_1016: defineMessage({
    description: 'Error code: ERR_1016',
    defaultMessage: 'Directory not accessible or not a directory',
    id: 'app.error.ERR_1016',
  }),
  ERR_1017: defineMessage({
    description: 'Error code: ERR_1017',
    defaultMessage: 'Cannot scan folder that is part of an existing library',
    id: 'app.error.ERR_1017',
  }),
  ERR_1018: defineMessage({
    description: 'Error code: ERR_1018',
    defaultMessage: 'File not found',
    id: 'app.error.ERR_1018',
  }),
  ERR_1019: defineMessage({
    description: 'Error code: ERR_1019',
    defaultMessage: 'Cannot import file that is part of an existing library',
    id: 'app.error.ERR_1019',
  }),
  ERR_1020: defineMessage({
    description: 'Error code: ERR_1020',
    defaultMessage: 'Book to upgrade does not belong to provided series',
    id: 'app.error.ERR_1020',
  }),
  ERR_1021: defineMessage({
    description: 'Error code: ERR_1021',
    defaultMessage: 'Destination file already exists',
    id: 'app.error.ERR_1021',
  }),
  ERR_1022: defineMessage({
    description: 'Error code: ERR_1022',
    defaultMessage: 'Newly imported book could not be scanned',
    id: 'app.error.ERR_1022',
  }),
  ERR_1023: defineMessage({
    description: 'Error code: ERR_1023',
    defaultMessage: 'Book already present in ReadingList',
    id: 'app.error.ERR_1023',
  }),
  ERR_1024: defineMessage({
    description: 'Error code: ERR_1024',
    defaultMessage: 'OAuth2 login error: no email attribute',
    id: 'app.error.ERR_1024',
  }),
  ERR_1025: defineMessage({
    description: 'Error code: ERR_1025',
    defaultMessage: 'OAuth2 login error: no local user exist with that email',
    id: 'app.error.ERR_1025',
  }),
  ERR_1026: defineMessage({
    description: 'Error code: ERR_1026',
    defaultMessage: 'OpenID Connect login error: email not verified',
    id: 'app.error.ERR_1026',
  }),
  ERR_1027: defineMessage({
    description: 'Error code: ERR_1027',
    defaultMessage: 'OpenID Connect login error: no email_verified attribute',
    id: 'app.error.ERR_1027',
  }),
  ERR_1028: defineMessage({
    description: 'Error code: ERR_1028',
    defaultMessage: 'OpenID Connect login error: no email attribute',
    id: 'app.error.ERR_1028',
  }),
  ERR_1029: defineMessage({
    description: 'Error code: ERR_1029',
    defaultMessage: 'ComicRack CBL does not contain any Book element',
    id: 'app.error.ERR_1029',
  }),
  ERR_1030: defineMessage({
    description: 'Error code: ERR_1030',
    defaultMessage: 'ComicRack CBL has no Name element',
    id: 'app.error.ERR_1030',
  }),
  ERR_1031: defineMessage({
    description: 'Error code: ERR_1031',
    defaultMessage: 'ComicRack CBL Book is missing series or number',
    id: 'app.error.ERR_1031',
  }),
  ERR_1032: defineMessage({
    description: 'Error code: ERR_1032',
    defaultMessage: 'EPUB file has wrong media type',
    id: 'app.error.ERR_1032',
  }),
  ERR_1033: defineMessage({
    description: 'Error code: ERR_1033',
    defaultMessage: 'Some entries are missing',
    id: 'app.error.ERR_1033',
  }),
  ERR_1034: defineMessage({
    description: 'Error code: ERR_1034',
    defaultMessage: 'An API key with that comment already exists',
    id: 'app.error.ERR_1034',
  }),
  ERR_1035: defineMessage({
    description: 'Error code: ERR_1035',
    defaultMessage: 'Error while getting EPUB TOC',
    id: 'app.error.ERR_1035',
  }),
  ERR_1036: defineMessage({
    description: 'Error code: ERR_1036',
    defaultMessage: 'Error while getting EPUB Landmarks',
    id: 'app.error.ERR_1036',
  }),
  ERR_1037: defineMessage({
    description: 'Error code: ERR_1037',
    defaultMessage: 'Error while getting EPUB page list',
    id: 'app.error.ERR_1037',
  }),
  ERR_1038: defineMessage({
    description: 'Error code: ERR_1038',
    defaultMessage: 'Error while getting EPUB divina pages',
    id: 'app.error.ERR_1038',
  }),
  ERR_1039: defineMessage({
    description: 'Error code: ERR_1039',
    defaultMessage: 'Error while getting EPUB positions',
    id: 'app.error.ERR_1039',
  }),
}
