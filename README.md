# Ooredoo Third-Party AI Security Assessment Platform

An Azure-deployable web app that runs the Ooredoo CIS GenAI/SaaS vendor security assessment end to end — questionnaire, evidence, AI-assisted analyst review, legal scope, board sign-off and reporting. The reference architecture is multi-cloud; the target deployment for this project is Azure.

## Layout

```
ooredoo_genai_vendor_security_questionnaire.json   # 135 controls, 15 sections, 9 risks
server/                                             # Node + Express API
client/                                             # React (Vite) SPA
```

## Roles

| Role | How they sign in | What they see |
| --- | --- | --- |
| Vendor | Local sign-in with invited email — **no SSO** | Overview, Questionnaire, Evidence Library, Submission Status |
| Security Analyst | Ooredoo SSO (Entra ID) | Assessment Overview, Control Review with AI validation, Risk Scoring, Gaps &amp; Findings |
| Legal Reviewer | Ooredoo SSO | Overview, Privacy &amp; Contracts (scoped to SEC-05/06/13/14), Observations |
| AI Risk Board | Ooredoo SSO | Risk Dashboard, AI Risk Report, Approval Workflow, Reports |
| Solution Architect | Ooredoo SSO | Architecture, Data Model, AI Agents, API Design, Workflow, Tech Stack, Deployment |

Vendor is the only role that can access the platform without SSO — enforced in the backend middleware (`X-Vendor-User` header for vendors; Entra Bearer token for every other role).

## Scoring model

- **Compliant** = 100 · **Partial** = 50 · **Non-Compliant** = 0
- **Not Applicable** is excluded from the weighted average
- Category score = weighted average of its controls; overall = category-weighted average
- Grade bands: 80–100 Low · 60–79 Medium · &lt;60 High

## Run locally

```bash
# Terminal 1
cd server
cp .env.example .env
npm install
npm run dev

# Terminal 2
cd client
cp .env.example .env
npm install
npm run dev
```

Open http://localhost:5173. The login screen has two sections: pick a staff role tile to browse the SSO views (mocked in dev), or enter a vendor email to sign in as an external vendor.

Answers persist in memory until the server restarts; uploaded evidence lands in `server/uploads/<vendorId>/`.

## Switching to real Entra ID + Azure services

1. **Cosmos DB** — create a database and container (partition key `/id`) and set `COSMOS_ENDPOINT`, `COSMOS_KEY`, `COSMOS_DB`, `COSMOS_CONTAINER` in `server/.env`.
2. **Blob Storage** — create a storage account and set `AZURE_STORAGE_CONNECTION_STRING`, `AZURE_BLOB_CONTAINER`. The container is created on first upload.
3. **Entra ID** — register an API app with app roles `analyst`, `legal`, `board`, `architect`; register (or reuse) an SPA app with the API scope granted. Assign users/groups to the roles in the Enterprise Application. Then set:
   - Server: `AUTH_MODE=entra`, `ENTRA_TENANT_ID`, `ENTRA_API_AUDIENCE`
   - Client: `VITE_AUTH_MODE=entra`, `VITE_ENTRA_CLIENT_ID`, `VITE_ENTRA_TENANT_ID`, `VITE_ENTRA_API_SCOPE`
4. In Entra mode, the vendor role still uses the local `X-Vendor-User` flow — external vendors don't need Entra accounts.

## Swapping the questionnaire

The default questionnaire is `ooredoo_genai_vendor_security_questionnaire.json` at the repo root. Point at a different file with `QUESTIONNAIRE_FILE=/path/to/file.json` in `server/.env`. The loader accepts either the Ooredoo shape (top-level `questions[]` grouped by `sectionId`) or a `sections[].questions[]` shape.

## API surface

| Method | Path | Roles | Purpose |
| --- | --- | --- | --- |
| GET | `/api/health` | public | Liveness |
| GET | `/api/me` | any signed-in | Current user + roles |
| GET | `/api/questionnaire` | any | Full questionnaire (categories, controls, AI verdicts, risks) |
| GET | `/api/questionnaire/legal` | any | Filtered to legal-tagged categories |
| GET | `/api/responses/mine` | vendor | Vendor's own draft |
| PUT | `/api/responses/mine` | vendor | Save/submit draft |
| GET | `/api/responses` | analyst, legal, board | List all responses |
| GET | `/api/responses/:vendorId` | analyst, legal, board | One response |
| POST | `/api/responses/:vendorId/review` | analyst, board | Add review note / decision |
| POST | `/api/artifacts/upload` | vendor | Upload evidence for a control |
| GET | `/api/artifacts/download` | any signed-in | Fetch artifact by opaque `location` |

## AI embeddings (RAG scaffold)

The recommendation and gap agents in the design need retrieval over the control catalog. The scaffold is in place:

- `server/src/embeddings/embedder.js` — provider-agnostic embedder. Providers: `none` (default, raises on use), `openai`, `vertex` (stub), `local` (via `@xenova/transformers`).
- `server/src/embeddings/store.js` — Postgres + pgvector. Ensures the extension + `control_embeddings` table (dim matches the provider's model).
- `server/scripts/embed-controls.js` — reads the questionnaire, builds a compact text per control (id + section + requirement + policy refs), embeds via the configured provider and upserts into pgvector.
- `POST /api/ai/similar-controls` — takes `{ controlId }` (looks up the stored vector) or `{ query }` (embeds on demand) and returns the top-k nearest controls.

### Bring it up

```bash
docker compose up -d pgvector          # brings up Postgres on localhost:5433

cd server
# In .env, set EMBEDDING_PROVIDER=openai and OPENAI_API_KEY (or pick another provider)
npm run embed                          # embeds all 135 controls
```

Rebuild the schema (e.g. after switching providers with a different dim):

```bash
npm run embed:rebuild
```

Adding a new provider: implement a class in `embedder.js` with `embed(texts): number[][]`, `dim`, `model`, and add it to the `createEmbedder` switch.

## Demo AI verdicts

The Control Validation, Recommendation, and Gap agents run through the LLM client (`server/src/llm/client.js`) — set `LLM_PROVIDER=anthropic` (direct) or `LLM_PROVIDER=foundry` (Azure AI Foundry hosting Claude) to enable them. Deterministic per-control demo data (seeded from the control id) is used as a fallback when the LLM is unset so the frontend still renders the purple validation panel, the orange recommendation panel and the override badge.
