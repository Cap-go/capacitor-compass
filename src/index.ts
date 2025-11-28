import { registerPlugin } from '@capacitor/core';

import type { CapgoCompassPlugin } from './definitions';

const CapgoCompass = registerPlugin<CapgoCompassPlugin>('CapgoCompass', {
  web: () => import('./web').then((m) => new m.CapgoCompassWeb()),
});

export * from './definitions';
export { CapgoCompass };
