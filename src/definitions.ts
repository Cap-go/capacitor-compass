/**
 * Result containing the compass heading value.
 *
 * @since 7.0.0
 */
export interface CompassHeading {
  /** Compass heading in degrees (0-360) */
  value: number;
}

/**
 * Event data for heading change events.
 *
 * @since 7.0.0
 */
export interface HeadingChangeEvent {
  /** Compass heading in degrees (0-360) */
  value: number;
}

/**
 * Permission state for compass access.
 *
 * @since 7.0.0
 */
export type PermissionState = 'prompt' | 'prompt-with-rationale' | 'granted' | 'denied';

/**
 * Permission status for compass plugin.
 *
 * @since 7.0.0
 */
export interface PermissionStatus {
  /**
   * Permission state for accessing compass/location data.
   * On iOS, this requires location permission to access heading.
   * On Android, no special permissions are required for compass sensors.
   *
   * @since 7.0.0
   */
  compass: PermissionState;
}

/**
 * Capacitor Compass Plugin interface for reading device compass heading.
 *
 * @since 7.0.0
 */
export interface CapgoCompassPlugin {
  /**
   * Get the current compass heading in degrees.
   * On iOS, the heading is updated in the background, and the latest value is returned.
   * On Android, the heading is calculated when the method is called using accelerometer and magnetometer sensors.
   * Not implemented on Web.
   *
   * @returns Promise that resolves with the compass heading
   * @throws Error if compass is not available or permission denied
   * @since 7.0.0
   * @example
   * ```typescript
   * const { value } = await CapgoCompass.getCurrentHeading();
   * console.log('Compass heading:', value, 'degrees');
   * ```
   */
  getCurrentHeading(): Promise<CompassHeading>;

  /**
   * Get the native Capacitor plugin version.
   *
   * @returns Promise that resolves with the plugin version
   * @throws Error if getting the version fails
   * @since 7.0.0
   * @example
   * ```typescript
   * const { version } = await CapgoCompass.getPluginVersion();
   * console.log('Plugin version:', version);
   * ```
   */
  getPluginVersion(): Promise<{ version: string }>;

  /**
   * Start listening for compass heading changes via events.
   * This starts the compass sensors and emits 'headingChange' events.
   *
   * @returns Promise that resolves when listening starts
   * @since 7.0.0
   * @example
   * ```typescript
   * await CapgoCompass.startListening();
   * CapgoCompass.addListener('headingChange', (event) => {
   *   console.log('Heading:', event.value);
   * });
   * ```
   */
  startListening(): Promise<void>;

  /**
   * Stop listening for compass heading changes.
   * This stops the compass sensors and stops emitting events.
   *
   * @returns Promise that resolves when listening stops
   * @since 7.0.0
   * @example
   * ```typescript
   * await CapgoCompass.stopListening();
   * ```
   */
  stopListening(): Promise<void>;

  /**
   * Add a listener for compass events.
   *
   * @param eventName - The event to listen for ('headingChange')
   * @param listenerFunc - The function to call when the event is emitted
   * @returns A promise that resolves with a handle to remove the listener
   * @since 7.0.0
   * @example
   * ```typescript
   * const handle = await CapgoCompass.addListener('headingChange', (event) => {
   *   console.log('Heading:', event.value, 'degrees');
   * });
   * // Later: handle.remove();
   * ```
   */
  addListener(
    eventName: 'headingChange',
    listenerFunc: (event: HeadingChangeEvent) => void,
  ): Promise<{ remove: () => Promise<void> }>;

  /**
   * Remove all listeners for this plugin.
   *
   * @returns Promise that resolves when all listeners are removed
   * @since 7.0.0
   * @example
   * ```typescript
   * await CapgoCompass.removeAllListeners();
   * ```
   */
  removeAllListeners(): Promise<void>;

  /**
   * Check the current permission status for accessing compass data.
   * On iOS, this checks location permission status.
   * On Android, this always returns 'granted' as no permissions are required.
   *
   * @returns Promise that resolves with the permission status
   * @since 7.0.0
   * @example
   * ```typescript
   * const status = await CapgoCompass.checkPermissions();
   * console.log('Compass permission:', status.compass);
   * ```
   */
  checkPermissions(): Promise<PermissionStatus>;

  /**
   * Request permission to access compass data.
   * On iOS, this requests location permission (required for heading data).
   * On Android, this resolves immediately as no permissions are required.
   *
   * @returns Promise that resolves with the new permission status
   * @since 7.0.0
   * @example
   * ```typescript
   * const status = await CapgoCompass.requestPermissions();
   * if (status.compass === 'granted') {
   *   // Can now use compass
   * }
   * ```
   */
  requestPermissions(): Promise<PermissionStatus>;
}
