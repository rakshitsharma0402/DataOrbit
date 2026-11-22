/**
 * DataOrbit Ingestion.
 *
 * <p>Owns: Initialize and Upload, attempt claims, verified STAGED metadata.
 *
 * <p>Must not own: Execute-time CSV validation or tier selection.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.ingestion;
