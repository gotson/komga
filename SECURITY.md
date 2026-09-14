# Security Policy

## Supported Versions

Only the latest release version of Komga receives security updates and bug fixes.

| Version | Supported          |
| ------- | ------------------ |
| Latest  | :white_check_mark: |
| Older   | :x:                |


## Reporting Guidelines & Rules

Before submitting a report, please review our requirements:

- **No AI-Generated Reports or Raw Automated Scanners:** Reports consisting of unvalidated output from AI models (LLMs), static analysis tools, or automated vulnerability scanners will be closed without review. Submissions must include a human-verified, functional Proof-of-Concept (PoC) demonstrating actual impact.
- **Private Disclosure:** Do **not** report security vulnerabilities through public GitHub Issues, Discussions, or Discord.


## How to Report

### Preferred Method: GitHub Private Vulnerability Reporting

The easiest and safest way to report a vulnerability is directly through GitHub:

1. Navigate to the [Komga Security Tab](https://github.com/gotson/komga/security).
2. Click **Report a vulnerability**.
3. Fill in the details of the issue and submit.

This creates a private thread between you and the project maintainers.

## What to Include in Your Report

To help triage your submission, please provide:

- **Description:** A detailed explanation of the issue and its potential impact.
- **Proof-of-Concept:** Clear, step-by-step reproduction instructions or a practical PoC (e.g., sample payload, specific API call sequence).
- **Environment:** Specific release version, installation type (Docker, Jar), and Java environment details.

## Disclosure & Credit

- **Fix & Release:** Validated vulnerabilities will be resolved in a private security branch prior to a patch release.
- **Attribution:** Maintainers are happy to credit reporter contributions in release notes and published GitHub Security Advisories upon request.
