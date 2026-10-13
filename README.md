# Fábrica Ideal
Projeto de MC322 por Glayson e Heitor

# Requisitos
- JDK 17+ para rodar o Gradle. Se não houver um JDK 25 na máquina, o Gradle baixa ele automaticamente (thanks foojay-resolver).

## Compilação e execução
Linux/macOS:
```
./gradlew build
./gradlew run
./gradlew javadoc
```

Windows:
```
gradlew.bat build
gradlew.bat run
gradlew.bat javadoc
```

# Novo procedimento de inserção de submenus
- A lista de opções do menu precisa ser uma lista de strings com as opções ordenadas. O último item da lista precisa ser a opção de Retornar para a página anterior ou Sair. `ConsolePrinter.optionsList(String... options)`. 
- Ao usar switch-case para selecionar métodos, colocar default para `menu.setLastBuffer(ConsolePrinter.failText(mensagem));`
- Usar try-catch para as funcionalidades principais e para acesso de array usando a entrada do usuário (`IndexOutOfBoundsException`)
