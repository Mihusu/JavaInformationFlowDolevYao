# Java Information Flow Dolev-Yao

This project is a Java compiler/interpreter that uses a fraction of it which will be utilized to make a small information-flow language named JIFDY. 
The language combines a security-label type system (`low`/`high`) with Dolev-Yao style message formats, encrypted values, send/receive statements, and simple Java-like control flow.

The compiler pipeline is:

1. ANTLR lexer/parser reads a `.jifdy` source file.
2. `ASTBuilder` converts the parse tree into AST nodes.
3. `TypeChecker` checks ordinary types, security labels, formats, ciphertexts, and legal information flow.
4. The AST can either be interpreted directly or compiled into `GeneratedProgram.java`.

## Requirements

- Java 17
- Maven
- ANTLR 4.13.2 dependencies, resolved through Maven

The Maven build already includes the custom source directories:

- `src/main/jifdy`
- `Math-language/src/main/antlr`

Do not move these directories unless you also update the build configuration.

## Important Files

- `src/main/jifdy/Information_flow.g4`  
  Main ANTLR grammar.

- `Math-language/src/main/antlr`  
  Generated ANTLR lexer/parser/visitor classes.

- `src/main/jifdy/Compiler.java`  
  Main compiler entry point. Currently reads `BankTransfer.jifdy`, typechecks it, generates `GeneratedProgram.java`, and interprets the program.

- `src/main/jifdy/ASTBuilder/ASTBuilder.java`  
  Converts parse tree nodes into AST nodes.

- `src/main/jifdy/ASTNodes`  
  AST nodes, type representations, expression evaluation, code generation, and runtime value classes.

- `src/main/jifdy/Analysis`  
  Type checking, environments, security checks, and regression tests.

- `src/test/resources/testfiles/LegalInformationFlow`  
  Programs expected to pass.

- `src/test/resources/testfiles/IlegalInformationFlow`  
  Programs expected to fail with type/security errors.

## Language Overview

JIFDY supports:

- primitive types: `int`, `bool`, `String`
- security labels: `low`, `high`
- class constructors with typed and labeled arguments:

```jifdy
class PatientRecord {
    String low patientName;
    int high patientId;

    public PatientRecord(String low name, int high id) {
        this.patientName = name;
        this.patientId = id;
    }
}

PatientRecord low record = new PatientRecord("Alice", 42);
```

- global keys:

```jifdy
key kClientBank;
```

- global message formats:

```jifdy
format Transfer1(String low user, int high amount, String low target);
```

- format values:

```jifdy
Transfer1 high transfer =
    Transfer1(user, amount1 + amount2, target);
```

- ciphertext values:

```jifdy
e(kClientBank,Transfer1) low msg =
    e(kClientBank, transfer);
```

- receiving encrypted patterns:

```jifdy
try_rcv(e(kClientBank,
    Transfer1(String low user, int high amount, String low target)
)) {
    print(user);
}
```

Formats are available inside ordinary expressions. Payloads can therefore use
variables, arithmetic expressions, and typed references. For example:

```jifdy
Transfer1(user, amount1 - amount2, target)
```

Receive patterns can use plain identifiers. Their types and security labels are
inferred from the declared format:

```jifdy
try_rcv(e(kClientBank,
    Transfer1(user, amount, target)
)) {
    print(user);
}
```

Older annotated references such as `String low user` remain accepted for
compatibility, but they are optional.

## Makefile

From the project root

```powershell
make
```
Which is to build, run compiler.java, tests, and run a generated program

To clean up the project from unecessary files:

```powershell
make clean
```

## Run Regression Tests

After building the runtime classpath:

```powershell
make test
```

The regression test runner checks:

- legal examples pass parsing and type checking
- illegal examples fail with expected type/security errors

Some parser diagnostics may still be printed for illegal examples, but the summary at the end is the important result:

```text
Tests Passed: 24
Tests Failed: 0
```

## IntelliJ Notes

If you use IntelliJ's ANTLR plugin, configure `Information_flow.g4` with:

- package/namespace: `antlr`
- generated output directory: `Math-language/src/main/antlr`

Keep the generated parser package as `antlr`, because the Java code imports:

```java
import antlr.Information_flowLexer;
import antlr.Information_flowParser;
```

## Development Notes

When changing grammar rules, usually update these areas together:

- `Information_flow.g4`
- generated parser files in `Math-language/src/main/antlr`
- `ASTBuilder.java`
- AST expression/format nodes if a new syntax form needs runtime behavior
- type checking logic if a new construct has type or label rules
- regression examples under `src/test/resources/testfiles`

Formats no longer have a separate grammar rule. Constructor calls and
encryption are parsed as expressions. At a `try_rcv(...)` boundary,
`ASTBuilder` converts the expression AST into pattern nodes such as
`EncryptFormat`, `ConstructorFormat`, and `TypedVarFormat`.

For format/ciphertext types, the code uses both precise and broad runtime types:

- `FormatType` identifies a specific declared format such as `Transfer1`.
- `CiphertextType` identifies a key/format pair such as `e(kClientBank,Transfer1)`.
- `Type.FORMAT` and `Type.CIPHERTEXT` are broad runtime categories used for Java generation and default values.
