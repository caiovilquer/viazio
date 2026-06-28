# Viazio (frontend)

Interface web do **Planejador de Feriadões**, consumindo exclusivamente a API REST versionada (`/api/v1`) do backend Spring Boot.

## Pré-requisitos

- [Node.js](https://nodejs.org/) 20+
- [pnpm](https://pnpm.io/)
- Backend rodando em `http://localhost:8080`

## Comandos

```bash
pnpm install   # instalar dependências
pnpm dev       # servidor de desenvolvimento (porta 5173)
pnpm build     # build de produção (tsc + vite build)
pnpm lint      # oxlint
pnpm test      # vitest (unitários)
pnpm preview   # servir o build localmente
```

O Vite faz proxy de `/api` para o backend. O CORS já permite `http://localhost:5173` (`app.cors.allowed-origins`).

## Stack

Vite, React 19, TypeScript, Tailwind CSS v4, shadcn/ui (componentes customizados), TanStack Query, React Router v7, Framer Motion.

Instruções completas do projeto (backend, testes, Docker, CI) estão no [README raiz](../README.md).
