import { cva, type VariantProps } from "class-variance-authority";

export const buttonVariants = cva(
  "group/button relative inline-flex shrink-0 items-center justify-center rounded-lg border border-transparent bg-clip-padding text-sm font-medium whitespace-nowrap transition-[transform,background-color,box-shadow,filter,border-color] duration-200 ease-[cubic-bezier(0.22,1,0.36,1)] outline-none select-none active:not-aria-[haspopup]:translate-y-px disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-2 aria-invalid:ring-destructive/30 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
  {
    variants: {
      variant: {
        default:
          "bg-primary text-primary-foreground shadow-[inset_0_1px_0_oklch(1_0_0/0.22)] hover:brightness-[1.07] hover:-translate-y-px active:brightness-[0.97]",
        gold: "bg-gold text-gold-foreground shadow-[inset_0_1px_0_oklch(1_0_0/0.28)] hover:brightness-[1.06] hover:-translate-y-px active:brightness-[0.97]",
        outline:
          "border-hairline bg-surface/40 text-foreground hover:bg-surface-2 hover:border-hairline aria-expanded:bg-surface-2",
        glass:
          "glass border-hairline text-foreground hover:bg-surface-3 aria-expanded:bg-surface-3",
        secondary:
          "bg-secondary text-secondary-foreground hover:bg-surface-3 aria-expanded:bg-surface-3",
        ghost:
          "text-muted-foreground hover:bg-surface-2 hover:text-foreground aria-expanded:bg-surface-2",
        destructive:
          "bg-destructive/12 text-destructive hover:bg-destructive/20",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default:
          "h-9 gap-1.5 px-3.5 has-data-[icon=inline-end]:pr-3 has-data-[icon=inline-start]:pl-3",
        xs: "h-6 gap-1 rounded-[min(var(--radius-md),10px)] px-2 text-xs in-data-[slot=button-group]:rounded-lg has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 [&_svg:not([class*='size-'])]:size-3",
        sm: "h-8 gap-1.5 rounded-[min(var(--radius-md),12px)] px-3 text-[0.8rem] in-data-[slot=button-group]:rounded-lg has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2 [&_svg:not([class*='size-'])]:size-3.5",
        lg: "h-11 gap-2 px-5 text-[0.95rem] has-data-[icon=inline-end]:pr-4 has-data-[icon=inline-start]:pl-4",
        xl: "h-12 gap-2 px-6 text-[0.95rem] has-data-[icon=inline-end]:pr-5 has-data-[icon=inline-start]:pl-5",
        icon: "size-9",
        "icon-xs":
          "size-6 rounded-[min(var(--radius-md),10px)] in-data-[slot=button-group]:rounded-lg [&_svg:not([class*='size-'])]:size-3",
        "icon-sm":
          "size-8 rounded-[min(var(--radius-md),12px)] in-data-[slot=button-group]:rounded-lg",
        "icon-lg": "size-11",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  },
);

export type ButtonVariantProps = VariantProps<typeof buttonVariants>;
