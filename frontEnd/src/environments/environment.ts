export const environment = {
  apiUrl: '/api/v1',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'library',
    clientId: 'library-front',
  },
} as const;
