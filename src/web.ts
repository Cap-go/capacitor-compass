import { WebPlugin } from '@capacitor/core';

import type { CapgoCompassPlugin, CompassHeading, HeadingChangeEvent, PermissionStatus } from './definitions';

export class CapgoCompassWeb extends WebPlugin implements CapgoCompassPlugin {
  async getCurrentHeading(): Promise<CompassHeading> {
    throw this.unavailable('Compass not available on web');
  }

  async getPluginVersion(): Promise<{ version: string }> {
    return { version: 'web' };
  }

  async startListening(): Promise<void> {
    throw this.unavailable('Compass not available on web');
  }

  async stopListening(): Promise<void> {
    throw this.unavailable('Compass not available on web');
  }

  /* eslint-disable @typescript-eslint/no-unused-vars */
  async addListener(
    _eventName: 'headingChange',
    _listenerFunc: (event: HeadingChangeEvent) => void,
  ): Promise<{ remove: () => Promise<void> }> {
    /* eslint-enable @typescript-eslint/no-unused-vars */
    throw this.unavailable('Compass not available on web');
  }

  async checkPermissions(): Promise<PermissionStatus> {
    return { compass: 'denied' };
  }

  async requestPermissions(): Promise<PermissionStatus> {
    throw this.unavailable('Compass not available on web');
  }
}
