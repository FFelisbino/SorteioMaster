# 🎉 Sorteio Master

App Android nativo para realizar sorteios transparentes a partir de comentários públicos do Instagram.

## Funcionalidades

- 🔗 **Busca por URL** — Cole o link de qualquer post público do Instagram
- ⚙️ **Filtros configuráveis**:
  - Menção obrigatória (`@usuario` que DEVE estar no comentário)
  - Mínimo de amigos marcados (0–5)
  - Permitir ou bloquear múltiplos comentários do mesmo usuário
  - Quantidade de vencedores reserva (0–5)
- 🎲 **Sorteio com SecureRandom** — Aleatoriedade criptograficamente segura (Fisher-Yates shuffle)
- 📊 **Estatísticas** — Total, únicos, participantes válidos após filtro
- 📋 **Histórico** — Todos os sorteios salvos localmente com Room DB
- 📤 **Compartilhar** — Resultado formatado para WhatsApp, Instagram, etc.

## Stack Técnica

| Camada | Tecnologia |
|--------|-----------|
| UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM (ViewModel + StateFlow) |
| Banco local | Room |
| HTTP | OkHttp 4 |
| Parsing | Jsoup + Gson |
| Linguagem | Kotlin 1.9 |
| Min SDK | Android 8.0 (API 26) |

## Como abrir no Android Studio

1. Abra o Android Studio (Hedgehog ou mais recente)
2. `File → Open` → selecione a pasta `SorteioMaster`
3. Aguarde o Gradle sync
4. Conecte um dispositivo ou inicie um emulador (API 26+)
5. Clique em ▶️ Run

## Estrutura do Projeto

```
app/src/main/java/com/sorteiomaster/
├── MainActivity.kt              # Entry point + navegação
├── data/
│   ├── model/
│   │   └── Models.kt            # Comentario, SorteioHistorico, UiState, enums
│   └── db/
│       └── Database.kt          # Room DAO + Database
├── domain/
│   ├── InstagramScraper.kt      # Busca comentários públicos via web
│   └── SorteioEngine.kt         # Filtros + sorteio com SecureRandom
└── ui/
    ├── SorteioViewModel.kt      # Estado + lógica de negócio
    ├── theme/
    │   └── Theme.kt             # Cores, tema Material 3
    └── screens/
        ├── TelaInicial.kt       # URL + filtros + botão sortear
        ├── TelaResultado.kt     # Vencedor + reservas + compartilhar
        └── TelaHistorico.kt     # Lista de sorteios anteriores
```

## Limitações Conhecidas

- **Posts privados** não são acessíveis (comportamento esperado)
- **Posts com comentários desativados** não retornam dados
- O Instagram pode limitar requisições muito frequentes — o scraper tem delay de 800ms entre páginas
- Máximo de ~1000 comentários por sorteio (20 páginas × 50) para evitar abusos

## Melhorias Futuras

- [ ] Exportar resultado como imagem
- [ ] Suporte a múltiplos posts no mesmo sorteio
- [ ] Verificar se o comentador segue o perfil (requer login OAuth)
- [ ] Animação de confetes na tela de resultado
- [ ] Widget de resultado para story do Instagram

## Licença

Projeto open-source — use, modifique e distribua à vontade.
