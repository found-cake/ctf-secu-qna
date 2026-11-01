# Secu Q&A UI

## 0. Research Log

- Reference: Intercom's restrained support-product layout and warm neutrals; adapted for a small internal ticket tool.
- Layout: document owns scrolling; header and ticket content remain in normal flow.
- Additional visual research was unnecessary for the requested utilitarian Thymeleaf UI.

## 1. Atmosphere & Identity

A quiet customer-support workspace with clear forms and readable ticket text. The signature is a narrow content column and simple rows, not decorative cards.

## 2. Color

Tokens: `--canvas: #faf9f6`, `--surface: #ffffff`, `--ink: #17212b`, `--muted: #5d6770`, `--line: #d8dedf`, `--accent: #234f76`, `--accent-hover: #173a59`, `--error: #a32929`.

## 3. Typography

System sans-serif. Page heading 32px, section heading 22px, body 16px, metadata 14px. Body line height 1.55.

## 4. Spacing & Layout

Base unit 4px. Main width 800px, form width 480px, page gutter 20px. Steps: 8, 12, 16, 24, 32, 48px. Document scrolls; ticket text wraps unbroken strings.

## 5. Components

- Header: brand, navigation, POST logout form. Links and button have focus states.
- Form field: label, control, error text. Controls remain full width on narrow screens.
- Button: primary and text variants; hover, active, focus, disabled states.
- Ticket row: subject link, category and date; empty list has a creation action.
- Content panel: ticket metadata and pre-wrapped, escaped body text.
- Footer notice: centered, muted 14px text with a top divider; follows the main content in document flow.

## 6. Motion & Interaction

Short color changes on controls. No decorative motion. All flows work without JavaScript.

## 7. Depth & Surface

Borders only. Inputs and buttons use 4px radius; panels use 6px radius.

## 8. Accessibility

Semantic landmarks, visible labels, clear focus rings, adequate contrast, and inline form errors. Ticket text and subjects wrap safely.
