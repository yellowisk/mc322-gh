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

## Dados persistidos

* **Matéria-prima**: quantidade, estoque mínimo, unidade e preço. Sem ela, a fábrica produziria e compraria com um estoque que não existe.
* **Máquinas e subcomponentes**: ordem na linha, configuração (capacidade, chance de falha base, custo de operação), saúde do motor, calibração dos sensores, resistências queimadas, desgaste da lâmina e lentes sujas. Salvamos o desgaste para evitar que as máquinas apareceram "renovadas" após cada reload do save, permitindo um tipo de "cheat".
* **Demandas**: quantidade pedida e produzida, status, tempo de produção e ordem de chegada. Decidimos salvar demandas, pois elas são algo que o usuário não gostaria de perder, evitando retrabalho. Nenhuma demanda é salva "Em Produção", pois este estado é transitório e apenas presente quando uma fabricação está em rodando, o usuário não vê este estado na tabela de demandas.
* **Cenário**: define o multiplicador de falha e o desgaste máximo com que as máquinas são remontadas, logo é importante. Além disso, não pode ser trocado depois de criada a planta.
* **Verba**: sem ela, reiniciar devolveria a verba inicial.
* **Estratégia**: mantém a escolha do usuário sobre qual demanda produzir.

**Observação:** Reparar custa verba, então, sem salvar o desgaste, reiniciar daria máquinas novas de graça.

### Dados mantidos apenas em memória

* **Estoque de produtos fabricados (armazém):** nenhuma regra usa esse estoque, visto que os produtos não são vendidos nem consumidos, só exibidos nos menus de armazém e auditoria. Além disso, a tarefa define como opcional o salvamento dos produtos.
* **Relatório de auditoria:** resume os produtos do armazém. Salvá-lo sem o armazém deixaria os dois inconsistentes. Além disso, a tarefa define como opcional o seu salvamento.
* **Catálogos (produtos, cenários, estratégias) e esteira:** são fixos no código.
* **Velocidade de produção:** preferência de exibição da animação, sem efeito nas regras.
* **Máquina ligada/desligada:** fora da produção, estão sempre desligadas.
* **Custo estimado das demandas:** é recalculado ao carregar.

**Limitação:** ao carregar, armazém e relatório começam vazios, então uma demanda pode mostrar 12/30 unidades produzidas com o armazém vazio.

## Suíte de Testes

Os testes usam JUnit 5 e rodam com `./gradlew test`. Cada teste monta os próprios dados, então nenhum depende da ordem de execução nem de uma execução anterior do programa. Nos testes de produção, a seed random é fixa e a animação fica desligada.

### Como visualizar os resultados dos testes
Ao rodar o comando `./gradlew test` é possível visualizar todos os logs dos testes. Para ter uma visualização mais bonita e resumida da execução da suíte de testes, é possível, após rodar o comando de execução dos testes, verificar o arquivo: `build/reports/tests/test/index.html`.

### Explicação da suíte de testes

* **Regras de negócio**, por pacote:
  - `model.machine` (`MotorTest`, `SensorTest`, um teste por tipo de máquina e `MaquinaRestauracaoTest`): desgaste e reparo das partes, quando a máquina precisa de manutenção e restauração do estado salvo.
  - `model.demand` (`DemandaTest`, `DemandaRestauracaoTest`): criação, custo e viabilidade, transições de status e restauração.
  - `model.rawmaterial`, `model.productionline` e `model.scenario`: consumo e reposição de matéria-prima, ligar, reparar e somar os custos da linha, e os cenários disponíveis.
  - `service` (`GerenciadorProducaoTest`, `FabricaPlantaTest`): a produção de uma demanda do início ao fim (desconto de vidro e verba, armazém, relatório, desgaste dos motores, demanda cancelada que continua de onde parou) e a montagem da fábrica.
  - `strategy` (`EstrategiasTest`, `CatalogoEstrategiasTest`): qual demanda cada estratégia escolhe.

* **Persistência** (`PersistenciaPlantaTest`):
  - Rodam numa pasta temporária (`@TempDir`), sem tocar no `dados/planta.xml` real.
  - Salvam, carregam e comparam campo a campo duas fábricas: a padrão e uma "alterada" (está no cenário Apocalíptico, tem partes desgastadas, uma demanda em cada status, estratégia Ferrari).
  - Conferem que salvar de novo substitui o save, que carregar cria objetos novos sem duplicar máquinas ou demandas e que uma demanda nova chega depois das carregadas.

* **Casos de exceção:** cada erro deve lançado usa alguma exceção específica, nunca uma `Exception` genérica. Essas específicas englobam:
  - **Regras de negócio:** valores fora do intervalo (ex.: calibração de sensor -0.1 ou 1.01, quando o permitido é só de 0.0 a 1.0), vidro ou verba insuficientes para produzir, máquina quebrada e transições de status proibidas.
  - **Persistência** (`PersistenciaPlantaErrosTest`): a maioria dos testes salva uma fábrica válida, estraga um trecho do XML e confere o tipo da exceção e sua mensagem. Além disso, verificamos que uma falha ao salvar não estraga o save anterior, e um carregamento com falha não altera a fábrica em uso. Os tipos de exceção ditam os casos de teste, que são:
    - Arquivo ausente, vazio ou corrompido/mal formatado: `ArquivoPersistenciaInexistenteException`, `ArquivoPersistenciaVazioException` ou `ArquivoPersistenciaCorrompidoException`.
    - Dados que quebram regras da fábrica (saúde acima de 100, máquina sem sensores, verba negativa, cenário ou produto desconhecido, demanda "Em Produção"): `DadosPersistidosInvalidosException`.
