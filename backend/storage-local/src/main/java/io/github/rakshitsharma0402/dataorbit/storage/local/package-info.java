/**
 * DataOrbit Storage Local.
 *
 * <p>Owns: Byte streaming, safe paths, checksum-verified copies, inspection and deletion on the local filesystem.
 *
 * <p>Must not own: Database lifecycle state and SQL transactions.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.storage.local;
