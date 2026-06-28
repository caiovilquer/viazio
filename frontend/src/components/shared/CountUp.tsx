import { useCountUp } from "./useCountUp";

export function CountUp({
  value,
  duration,
  suffix,
  className,
}: {
  value: number;
  duration?: number;
  suffix?: string;
  className?: string;
}) {
  const v = useCountUp(value, duration);
  return (
    <span className={className}>
      {v}
      {suffix}
    </span>
  );
}
