# Coding Conventions for KMP/CMP Project

## Kotlin/Gradle Rules
- No hardcoded strings or values - use `enum`, `object`, or separate classes
- Avoid unnecessary blank lines in code
- For functions with multiple parameters, put each parameter on its own line
- Use `=` for function body or property when possible (single expression)
- Never use `!!` (non-null assertion) - use safe calls `?.` or `let {}`
- Prefer `val` over `var`
- Use sealed classes/enums for state representation

## Compose Multiplatform Rules
- Keep composables small and focused
- Use `@Composable` only on UI functions
- Extract state to ViewModels or holders
- Use `remember` and `derivedStateOf` appropriately
- use .claude skills
- allways use version catalog
- to search for multiplatform libs use klibs.io and find the best match for all architectures, mainly android and linux
- allways fix missing dependencies
- do not create necessary files, for manuals create simple md file and manual, and never in another than english language
- allways test project is buildable through Gradle with no questions, no lint questions
- do not commit until project is error-safe and error-clear
- allways check all imports are ok and lint does not fail

## Project Structure
- Shared code in `:shared` or `:core` module
- Platform-specific implementations in `android/`, `desktop/`, `ios/`
- Use expect/actual for platform APIs
- never remove commented code
- if changing code or function or method or member or const, comment old and make new
- do not remove comments
- do not remove old code, made new class, or new function, or new member, or new const instead
- allways use compact and coherent project structure
- allways check already implemented logic / functionality / features
- if fix requested watch all 3 latest pushes to main repository, compare diffs and made best decision to merge logic / code

## Code style
- allways for every class one file, one class one file, no inheritance
- allways for every enum file, one enum one file, no inheritance
- allways for every object file, one object one file, no inheritance
- allways for constants made one object file, one object one file, no inheritance
- do not hardcode strings or constants
- no magic fields or magic numbers
- keep code clean
- every function or method, or class, or enum or object has to be commented
- all comments must be in english

## Gradle KTS
- Use `libs.versions.toml` for dependency versions
- No hardcoded version numbers in build files
- Organize dependencies by implementation/testImplementation/etc.

## Lint errors
- automatically correct all lint errors

## Checks on every commit
- project must be buildable / runnable and run action for IntelliJ IDEA must work, and deb must be releasable

## Commits
- allways use one commit for one fix / prompt
- Use one line, no cowork details, no any details in commits, only one line with feature/fix without feature and or fix word, no ":"
- do not commit message and details, no co-authored strings in repo
- all commits must be in english

