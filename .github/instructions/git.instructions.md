---
description: Describe when these instructions should be loaded
# applyTo: 'Describe when these instructions should be loaded' # when provided, instructions will automatically be added to the request context when the pattern matches an attached file
---
Provide project context and coding guidelines that AI should follow when generating code, answering questions, or reviewing changes.

Branch
 - new branch for each new feature, bug fix, or improvement
 - descriptive branch names (e.g., feature/add-user-authentication, bugfix/fix-login-error)
 - regularly pull changes from main (and develop - this working branch) to keep the branch up-to-date 
 - develop is the main working branch where all features and fixes are merged before going to main. Always create pull requests to develop, never to main.

Commit Messages
  - use present tense and be concise (e.g., "Add user authentication", "Fix login error")
  - include a brief description of the change and its purpose
  - reference related issues or pull requests when applicable (e.g., "Fixes #123", "Related to #456")

Pull Requests
  - create a pull request for each branch when the feature or fix is complete
  - provide a clear and detailed description of the changes made
  - include screenshots or GIFs if the change affects the UI
  - request reviews from team members and address feedback promptly
  - always in Spanish, never in English.

Best Practices
  - ensure code is well-tested and follows the project's coding standards
  - avoid committing large files or sensitive information (e.g., passwords, API keys)
  - use .gitignore to exclude files that should not be tracked
  - regularly clean up branches that have been merged to keep the repository organized
  - communicate with the team about ongoing work and potential conflicts to minimize merge issues
  - always review your changes before committing and ensure that your commit messages accurately reflect the changes made. This helps maintain a clear project history and facilitates collaboration among team members.
  - follow the project's coding style and conventions (e.g., naming conventions, indentation)
  - write clear and maintainable code, with comments where necessary to explain complex logic

IMPORTANT: Always review your changes before committing and ensure that your commit messages accurately reflect the changes made. This helps maintain a clear project history and facilitates collaboration among team members.

Always do Pull Request to the develop branch, never to main.
Language: The code always in Java, Spring Boot, and related technologies in English.
