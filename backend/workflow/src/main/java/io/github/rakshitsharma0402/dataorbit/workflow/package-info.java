/**
 * DataOrbit Workflow.
 *
 * <p>Owns: Processing sequence, jobs, controlled retry and recovery decisions.
 *
 * <p>Must not own: Parser-specific syntax and direct filesystem path construction.
 *
 * @see <a href="https://github.com/rakshitsharma0402/DataOrbit/blob/main/docs/book/chapters/05-the-modular-monolith-and-its-boundaries.md">Handbook chapter 05</a>
 */
package io.github.rakshitsharma0402.dataorbit.workflow;
