# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**sfweath** is a Clojure application that fetches National Weather Service Area Forecast Discussion (AFD) data, sends it to OpenAI's GPT-4o API for summarization in Richard Feynman's speaking style, and distributes the summary via Telegram. It runs automatically daily via GitHub Actions.

## Development Commands

### Running the Application
```bash
lein run sfweath.core
```

### Interactive Development
Start a REPL:
```bash
lein repl
```

The REPL namespace is initialized to `sfweath.core`. The codebase includes cider-nrepl for enhanced REPL support.

### Required Environment Variables
- `OPENAI_API_KEY` - OpenAI API authentication (required)
- `TELEGRAM_BOT_TOKEN` - Telegram bot token (required)
- `TELEGRAM_CH` - Telegram channel ID for sending messages (optional)
- `SEND_PROBABILITY` - Float 0.0-1.0 for probabilistic Telegram sending (optional, default: 1.0)

## Architecture

### Data Flow
```
NWS Website (MTR/AFD) → HTML Parse → OpenAI GPT-4o → Summary → Telegram + File Output
```

1. **Fetch**: `core.clj` fetches AFD from `forecast.weather.gov` using enlive HTML parser
2. **Clean**: Removes "&&" delimiters from AFD text
3. **Summarize**: `openai.clj` sends cleaned text to GPT-4o with Feynman persona from `setup.txt`
4. **Distribute**: Saves to `afd` and `afd.sum` files, optionally sends to Telegram via `telegram.clj`
5. **Commit**: GitHub Actions commits generated files back to repository

### Module Organization

**`src/sfweath/core.clj`**: Main orchestration
- Uses top-level `def` for eager evaluation (executes on namespace load)
- Fetches HTML, extracts text, coordinates OpenAI and Telegram interactions
- `-main` function handles file writing and Telegram sending logic

**`src/sfweath/openai.clj`**: OpenAI GPT integration
- Configures GPT-4o model with temperature 1.2 for creative output
- Loads system prompt from `setup.txt` (Feynman persona instructions)
- Loads user prompt from `prompt.txt`
- Returns structured response via cheshire JSON parsing

**`src/sfweath/telegram.clj`**: Telegram Bot API wrapper
- Provides `send-message` function for posting summaries to channels
- HTTP request handling for Telegram API endpoints

### Automated Execution

GitHub Actions workflow (`.github/workflows/update.yaml`):
- **Schedule**: Daily at 5:03 PM UTC (9:03 AM PST) via cron
- **Manual trigger**: Available via `workflow_dispatch`
- **Process**: Runs `lein run`, commits `afd` and `afd.sum` files, pushes to main branch
- **Bot commits**: Uses github-actions bot identity for automated commits

## Key Implementation Details

### HTML Parsing with Enlive
The application uses Enlive's selector syntax to extract AFD text:
```clojure
(html/texts (html/select page [:pre.glossaryProduct]))
```

### Eager Evaluation Pattern
Top-level `def` declarations execute immediately when namespace loads. This means:
- `page`, `text`, `r-body`, and `summary` are computed on namespace initialization
- REPL experimentation should be done in comment blocks or by reloading the namespace

### GPT Persona Configuration
The `setup.txt` file contains critical instructions for GPT behavior:
- Roleplay as Richard Feynman (without mentioning the name)
- Target ~40 words for summaries
- Include Russian phrase "Чую, что скоро развергнутся хляби небесные!" when rain is forecasted
- Highlight critical weather phenomena (snow, thunderstorms, high winds)

### Probabilistic Telegram Sending
The `SEND_PROBABILITY` environment variable enables testing and controlled notification frequency:
```clojure
(if (and (some? telegram-channel)
         (<= (rand) send-probability))
  (send-message ...))
```

## Dependencies

- **enlive** (1.1.6) - HTML parsing and CSS selector-based extraction
- **clj-http** (3.12.3) - HTTP client for API requests
- **cheshire** (5.11.0) - JSON encoding/decoding for OpenAI API
- **org.clojure/tools.reader** (1.3.6) - Clojure reader utilities

## Regional Configuration

The application is specifically configured for San Francisco Bay Area weather:
- NWS site: MTR (San Francisco/Monterey Weather Forecast Office)
- AFD product type focuses on detailed meteorological discussions for this region

To adapt for different regions, modify `base-url` in `core.clj` with appropriate NWS site code.
