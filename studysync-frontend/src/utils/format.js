/**
 * Formats a "HH:mm" or "HH:mm:ss" 24-hour time string into a 12-hour
 * AM/PM display string, e.g. "06:00:00" -> "6:00 AM".
 */
export function formatTime12h(timeStr) {
  if (!timeStr) return '';
  const [hStr, mStr] = timeStr.split(':');
  let h = parseInt(hStr, 10);
  const m = mStr;
  const period = h >= 12 ? 'PM' : 'AM';
  h = h % 12;
  if (h === 0) h = 12;
  return `${h}:${m} ${period}`;
}

export function formatTimeRange12h(startStr, endStr) {
  return `${formatTime12h(startStr)} - ${formatTime12h(endStr)}`;
}
