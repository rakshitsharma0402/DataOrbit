/**
 * DataOrbit Classification.
 *
 * <p>Owns: Pure tier choice for size and immutable policy.
 *
 * <p>Must not own: I/O, SQL or mutable application configuration. Receives its policy by injection; never depends on configuration.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.classification;
