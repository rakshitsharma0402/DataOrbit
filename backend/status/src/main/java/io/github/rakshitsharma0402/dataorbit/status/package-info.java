/**
 * DataOrbit Status.
 *
 * <p>Owns: Operational persistence contracts, jobs, events and tasks, read-only status.
 *
 * <p>Must not own: The business decision of whether data is valid or where it belongs.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.status;
