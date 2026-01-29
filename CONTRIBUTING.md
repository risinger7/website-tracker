# Contributing Guide

## Branch Strategy

We use a git flow-like strategy:

- **main**: Production-ready code
- **develop**: Development branch for features and fixes
- **feature/\***: Feature branches created from `develop`
- **bugfix/\***: Bug fix branches created from `develop`

## Making Changes

1. Create a feature branch from `develop`:

    ```bash
    git checkout develop
    git pull origin develop
    git checkout -b feature/your-feature-name
    ```

2. Make your changes and commit with clear messages:

    ```bash
    git commit -m "feat: add new feature description"
    ```

3. Push your branch:

    ```bash
    git push origin feature/your-feature-name
    ```

4. Create a Pull Request on GitHub targeting the `develop` branch

## Commit Message Convention

Follow conventional commits:

- `feat:` for new features
- `fix:` for bug fixes
- `docs:` for documentation changes
- `refactor:` for code refactoring
- `test:` for test changes
- `chore:` for build, dependencies, etc.

Example: `feat: add user authentication module`

## Pull Request Process

1. Ensure your branch is up to date with `develop`
2. Fill out the PR template completely
3. Link related issues
4. Ensure all tests pass
5. Request review from team members
6. Address feedback and make requested changes
7. Once approved, squash and merge to `develop`

## Code Review

- Be respectful and constructive in feedback
- Test changes locally before approving
- Ensure code follows project conventions
- Check for performance and security issues

## Running Tests

```bash
mvn test
```

## Building the Project

```bash
mvn clean install
```
