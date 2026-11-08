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

- Criar uma subclasse de `Submenu` (implementando `icone()`, `rotulo()` e `exibir()`) e adicioná-la na lista `submenus` do `Menu`.
- A lista de opções do menu precisa ser uma lista de strings com as opções ordenadas. O último item da lista precisa ser a opção de Retornar para a página anterior ou Sair. `ConsolePrinter.listaOpcoes(String... opcoes)`.
- Ao usar switch-case para selecionar métodos, colocar default para `menu.setUltimaMensagem(ConsolePrinter.textoFalha(mensagem));`

# Escopo e Limitações

## 1. Dados persistidos vs. não persistidos

O estado da fábrica fica em `dados/planta.xml` e é salvo, carregado e atualizado pelo menu "Salvar e carregar".

### Dados persistidos

* **Matéria-prima**: quantidade, estoque mínimo, unidade e preço. Sem ela, a fábrica produziria e compraria com um estoque que não existe.
* **Máquinas e subcomponentes**: ordem na linha, configuração (capacidade, chance de falha base, custo de operação), saúde do motor, calibração dos sensores, resistências queimadas, desgaste da lâmina e lentes sujas. Reparar custa verba, então, sem salvar o desgaste, reiniciar daria máquinas novas de graça.
* **Demandas**: quantidade pedida e produzida, status, tempo de produção e ordem de chegada. Evita perder demandas ou produzi-las duas vezes, uma demanda cancelada volta de onde parou. Nenhuma demanda é salva "Em Produção"
* **Cenário**: define o multiplicador de falha e o desgaste máximo com que as máquinas são remontadas, e não pode ser trocado depois de criada a planta.
* **Verba**: sem ela, reiniciar devolveria a verba inicial.
* **Estratégia**: mantém a escolha do usuário sobre qual demanda produzir.

### Dados mantidos apenas em memória

* **Estoque de produtos fabricados (armazém):** nenhuma regra usa esse estoque. Os produtos não são vendidos nem consumidos, só exibidos nos menus de armazém e auditoria. O progresso que importa (unidades feitas por demanda) já é salvo na demanda.
* **Relatório de auditoria:** resume os produtos do armazém. Salvá-lo sem o armazém deixaria os dois inconsistentes.
* **Catálogos (produtos, cenários, estratégias) e esteira:** são fixos no código. O XML guarda só o nome do cenário e da estratégia, e um nome desconhecido é dado inválido.
* **Velocidade de produção:** preferência de exibição da animação, sem efeito nas regras.
* **Máquina ligada/desligada:** fora da produção, estão sempre desligadas.
* **Custo estimado das demandas:** é recalculado ao carregar.
* **Semente do gerador aleatório:** as falhas são sorteadas a cada execução.

**Limitação:** ao carregar, armazém e relatório começam vazios, então uma demanda pode mostrar 12/30 unidades produzidas com o armazém vazio.

## 2. Explicação da suíte de testes

Os testes usam JUnit 5 e rodam com `./gradlew test` (ou `./gradlew build`). Cada teste confere a saída ou a exceção esperada para entradas válidas, inválidas e nos extremos do que é permitido.

* **Regras de negócio:** testam a criação dos objetos, o desgaste e o reparo das máquinas e partes, o consumo e a reposição de matéria-prima, as transições de status das demandas e cada estratégia (ordem de chegada, maior demanda e viabilidade). Por exemplo, ao restaurar a calibração de um sensor (permitido de 0.0 a 1.0), 0.0 é aceito, mas -0.1 e 1.01 lançam exceção.
* **Persistência** (`PersistenciaPlantaTest`):
  - Rodam numa pasta temporária (`@TempDir`), sem tocar no `dados/planta.xml` real.
  - Salvam, carregam e comparam campo a campo duas fábricas: a padrão e uma "alterada", sem nenhum valor padrão (cenário Apocalíptico, partes desgastadas, uma demanda em cada status, estratégia Ferrari).
  - Conferem que salvar de novo substitui o save, que carregar cria objetos novos sem duplicar máquinas ou demandas e que uma demanda nova chega depois das carregadas.
* **Casos de exceção:** cada erro deve lançar sua exceção específica, nunca uma `Exception` genérica.
  - **Regras de negócio:** valores negativos, acima do limite e transições de status proibidas.
  - **Persistência** (`PersistenciaPlantaErrosTest`): a maioria dos testes salva uma fábrica válida, estraga um trecho do XML e confere o tipo da exceção e se a mensagem aponta o erro.
    - Arquivo ausente, vazio ou malformado (não é XML, cortado, sem tag ou atributo, número inválido, versão desconhecida): `ArquivoPersistenciaInexistenteException`, `ArquivoPersistenciaVazioException` ou `ArquivoPersistenciaCorrompidoException`.
    - Dados que quebram regras da fábrica (saúde acima de 100, máquina sem sensores, verba negativa, cenário ou produto desconhecido, demanda "Em Produção"): `DadosPersistidosInvalidosException`.
    - Uma falha ao salvar não estraga o save anterior, e um carregamento com falha não altera a fábrica em uso.
