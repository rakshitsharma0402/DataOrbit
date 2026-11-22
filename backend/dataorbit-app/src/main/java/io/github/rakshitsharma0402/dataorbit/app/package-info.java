/**
 * DataOrbit Application.
 *
 * <p>Owns: Executable entry point, REST adapters, dependency composition and concrete database adapters.
 *
 * <p>Must not own: CSV business rules or direct low-level storage policy. Assembly only.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.app;
