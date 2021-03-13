export function parseSummary (summary: string): string {
  // convert new lines to html line breaks
  return summary.replace(/\r\n/g, '<br />').replace(/\n/g, '<br />');
}
