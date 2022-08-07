# codroid-textmate

[![check](https://github.com/zacharychin233/codroid-textmate/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/zacharychin233/codroid-textmate/actions/workflows/ci.yml) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codroid/codroid-textmate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codroid/codroid-textmate) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)[![Docs](https://img.shields.io/badge/docs-latest-informational)](https://zacharychin233.github.io/codroid-textmate/)

**<u>Kotlin</u>** port of [vscode-textmate](https://github.com/microsoft/vscode-textmate/tree/caab3de34a8cc7182141c9e31e0f42b96a3a1bac). It helps tokenize text using [TextMate grammars.](https://macromates.com/manual/en/language_grammars)

[Regex](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/) in kotlin-stdlib is used for parsing regexps by default. However, it may not be accurate because of the uniformity issue. So you can implement the `RegexLib` interface with [Oniguruma](https://github.com/kkos/oniguruma), or use [oniguruma-lib](https://github.com/zacharychin233/codroid-textmate/tree/master/oniguruma-lib) directly.

## Usage

#### 1. Add dependencies

For `build.gradle.kts`:

```kotlin
dependencies { 
    implementation("org.codroid:codroid-textmate:1.0.0")
    
    // Optional but recommended.
    implementation("org.codroid:oniguruma-lib:1.0.0")
}
```

#### 2. Write code

```kotlin
suspend fun main(args: Array<String>): Unit = withContext(Dispatchers.IO) {
    // Grammar textmate file: https://github.com/nishtahir/language-kotlin/blob/master/dist/Kotlin.tmLanguage
    {}.javaClass.getResourceAsStream("Kotlin.tmLanguage")?.let { input ->

        // Create a registry that can create a grammar from a scope name.
        val registry = Registry(RegistryOptions(
            regexLib = OnigLib(), // You must add the oniguruma-lib dependency, or use StandardRegex()
            loadGrammar = {
                if (it == "source.kotlin") {
                    // It only accepts json and plist file.
                    return@RegistryOptions parseRawGrammar(input, "./Kotlin.tmLanguage")
                }
                return@RegistryOptions null
            }
        ))

        // Load the Kotlin grammar
        registry.loadGrammar("source.kotlin").run {
            val lines = arrayOf(
                "fun sayHello(name) {",
                "\t println(\"Hello \$name !\")",
                "}"
            )
            var ruleStack = StateStack.Null
            for (line in lines) {
                val result = tokenizeLine(line, ruleStack, 0)
                println("\nTokenizing line: $line")
                result.tokens.forEach { token ->
                    println(
                        " - Token from ${token.startIndex} to ${token.endIndex} ( ${
                            line.substring(
                                token.startIndex,
                                token.endIndex
                            )
                        } ) with scopes ${token.scopes.joinToString(", ")}"
                    )
                }
                ruleStack = result.ruleStack
            }
        }
    }
}
```

If everything goes well, your output is as follows.

```text
Tokenizing line: fun sayHello(name) {
 - Token from 0 to 3 ( fun ) with scopes source.kotlin, meta.function.kotlin, keyword.other.kotlin
 - Token from 3 to 4 (   ) with scopes source.kotlin, meta.function.kotlin
 - Token from 4 to 12 ( sayHello ) with scopes source.kotlin, meta.function.kotlin, entity.name.function.kotlin
 - Token from 12 to 13 ( ( ) with scopes source.kotlin, meta.function.kotlin, meta.parameters.kotlin, punctuation.section.group.begin.kotlin, punctuation.definition.parameters.begin.kotlin
 - Token from 13 to 17 ( name ) with scopes source.kotlin, meta.function.kotlin, meta.parameters.kotlin
 - Token from 17 to 18 ( ) ) with scopes source.kotlin, meta.function.kotlin, meta.parameters.kotlin, punctuation.section.group.end.kotlin, punctuation.definition.parameters.end.kotlin
 - Token from 18 to 19 (   ) with scopes source.kotlin, meta.function.kotlin
 - Token from 19 to 20 ( { ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, punctuation.section.group.begin.kotlin

Tokenizing line: 	 println("Hello $name !")
 - Token from 0 to 9 ( 	 println ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin
 - Token from 9 to 10 ( ( ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, punctuation.section.group.begin.kotlin
 - Token from 10 to 11 ( " ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, string.quoted.double.kotlin, punctuation.definition.string.begin.kotlin
 - Token from 11 to 17 ( Hello  ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, string.quoted.double.kotlin
 - Token from 17 to 22 ( $name ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, string.quoted.double.kotlin, entity.string.template.element.kotlin
 - Token from 22 to 24 (  ! ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, string.quoted.double.kotlin
 - Token from 24 to 25 ( " ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, string.quoted.double.kotlin, punctuation.definition.string.end.kotlin
 - Token from 25 to 26 ( ) ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, meta.group.kotlin, punctuation.section.group.end.kotlin

Tokenizing line: }
 - Token from 0 to 1 ( } ) with scopes source.kotlin, meta.function.kotlin, meta.block.kotlin, punctuation.section.group.end.kotlin

```

## License

[MIT](https://github.com/zacharychin233/codroid-textmate/blob/master/LICENSE)