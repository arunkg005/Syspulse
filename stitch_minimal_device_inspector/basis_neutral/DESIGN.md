# The Design System: Editorial Minimalism for Android

## 1. Overview & Creative North Star: "The Digital Gallery"
This design system rejects the "boxed-in" nature of standard mobile frameworks. Our Creative North Star is **The Digital Gallery**: an experience that treats content as curated art and the UI as the silent, sophisticated architecture that holds it. 

We move beyond "standard" Android layouts by utilizing **Intentional Asymmetry** and **Tonal Depth**. By removing traditional dividers and borders, we force the user’s eye to follow a hierarchy driven purely by scale, weight, and subtle shifts in surface luminance. The result is a high-end, editorial feel that feels expensive, quiet, and profoundly intentional.

---

## 2. Colors & Surface Architecture
The palette is rooted in a monochromatic spectrum, using functional colors only for critical feedback. We achieve premium quality through "Tonal Layering" rather than structural ornamentation.

### The "No-Line" Rule
**Prohibit 1px solid borders for sectioning.** Physical lines create visual clutter. Instead, boundaries must be defined solely through background color shifts. For instance, a `surface-container-low` section sitting on a `surface` background provides all the definition a user needs without the "cheapness" of a stroke.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. Use the following tiers to define importance:
- **Base Layer:** `surface` (#f9f9fc) — The canvas of the application.
- **Sectioning:** `surface-container-low` (#f2f3f7) — Used to group large content areas.
- **Interactive Cards:** `surface-container-lowest` (#ffffff) — These are the "hero" containers that sit atop the base layer, creating a "lifted" feel through contrast.
- **Nesting:** If a card requires an internal grouped element, use `surface-container` (#ebeef3) to "recede" that element into the card.

### Functional Tones
- **Success:** Use `primary` (#5d5e61) for neutral success or custom green tokens for status.
- **Error:** `error` (#9f403d) on `on-error` (#fff7f6). Use `error-container` (#fe8983) for soft backgrounds.
- **Warning:** Use `tertiary` (#545f78) for a sophisticated, non-vibrant warning state that aligns with the neutral aesthetic.

---

## 3. Typography: The Editorial Scale
Typography is our primary tool for navigation. We use the **Inter** typeface to maintain a clean, modernist look, but we manipulate its scale to create "high-contrast" entry points.

- **Display (The Statement):** Use `display-md` (2.75rem) for empty states or welcome headers. It should feel unapologetically large.
- **Headline (The Anchor):** `headline-sm` (1.5rem) is used for screen titles in the Top App Bar.
- **Title (The Narrative):** `title-lg` (1.375rem) defines the start of a new card or content section.
- **Body (The Information):** `body-md` (0.875rem) is the workhorse. Ensure a line-height that allows the text to "breathe."
- **Label (The Utility):** `label-md` (0.75rem) in `on-surface-variant` (#596066) for secondary data.

**Editorial Rule:** Never center-align long-form content. Use left-alignment to maintain the "strong vertical axis" characteristic of high-end print media.

---

## 4. Elevation & Depth
In this system, elevation is a psychological state, not a shadow effect.

- **The Layering Principle:** Depth is achieved by stacking. A `surface-container-lowest` card placed on a `surface-container-high` background creates a natural, sharp pop.
- **Ambient Shadows:** Shadows are rarely used. When necessary (e.g., a floating Action Button), use a `4%` opacity of the `on-surface` color with a `32px` blur. It should be felt, not seen.
- **The "Ghost Border" Fallback:** If a container lacks contrast against its background (common in Dark Mode), use a "Ghost Border": `outline-variant` (#acb3b9) at **12% opacity**. This provides a hint of a boundary without breaking the "No-Line" rule.

---

## 5. Components

### Cards & Lists
*   **The Card:** Must use `roundedness.lg` (0.5rem). No borders. Separation is achieved through vertical white space (use 24px or 32px gaps).
*   **The List:** Forbid divider lines. Use `surface-container-low` as a background for every second item if a "zebra-stripe" is needed, but preference is for generous padding (16px vertical).

### Buttons
*   **Primary:** `primary` (#5d5e61) background with `on-primary` (#f7f7fa) text. Shape: `roundedness.md`.
*   **Secondary:** `surface-container-highest` background. Subtle, tonal, and integrated.
*   **Tertiary:** Ghost style. No background, `primary` text. Use for low-emphasis actions like "Cancel."

### Input Fields
*   **Style:** Filled style using `surface-container-high`. No bottom line.
*   **Focus:** Transition the background to `surface-container-highest` and add a 1px `primary` ghost-border (20% opacity).

### Top App Bar
*   **Composition:** A rigid, 64dp tall container. 
*   **Title:** `title-lg` left-aligned.
*   **Action:** The Light/Dark toggle sits in the top right, using a `surface-container-lowest` circular base (`roundedness.full`).

---

## 6. Do’s and Don'ts

### Do:
- **Embrace White Space:** If you think there is enough padding, add 8px more. White space is a functional element that directs the eye.
- **Use Tonal Contrast:** In Dark Mode, rely on the shift from `surface` to `surface-container-high` to define card areas.
- **Type-First Hierarchy:** Make sure the difference between a Title and Body is immediately obvious through weight and size.

### Don't:
- **No Keylines:** Never use a 100% opaque hex code for a border. 
- **No Pure Black:** Even in Dark Mode, use the dark grey tokens provided in the `surface` scales to maintain "inkiness" and prevent OLED black-smearing.
- **No Decorative Icons:** Icons must serve a functional purpose. Do not use icons as "bullet points" for text; let the typography handle the structure.