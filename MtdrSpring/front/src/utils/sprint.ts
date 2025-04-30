import { getSprints } from "../api/sprint";
import { Sprint } from "../models/Sprint";

/**
 *
 * @param from
 * @param to
 * @param current
 * @returns boolean if current is between from and to variables
 */

function isBetweenDates(from: Date, to: Date, current: Date) {
  // ?? https://stackoverflow.com/questions/2627650/why-javascript-gettime-is-not-a-function

  return (
    current.getTime() <= new Date(to).getTime() &&
    current.getTime() >= new Date(from).getTime()
  );
}

export async function getCurrentSprint(): Promise<Sprint | undefined> {
  const sprints = await getSprints();
  if (!sprints) return;

  for (const sprint of sprints) {
    // ?? Should consider early return instead.
    if (sprint.startsAt && sprint.endsAt) {
      const current = new Date();
      if (isBetweenDates(sprint.startsAt, sprint.endsAt, current)) {
        return sprint;
      }
    }
  }

  console.log("No Current Sprint found.");
  return;
}

/**
 *
 * @param selected
 * @param current
 * @returns boolean if current date is after the one selected.
 */

function isBeforeDate(selected: Date, current: Date): boolean {
  return new Date(selected).getTime() <= current.getTime();
}

export async function isSelectedSprintExpired(
  selectedSprint: Sprint,
): Promise<boolean> {
  if (!selectedSprint.endsAt) return false;

  const current = new Date();

  // ?? If the date is before now, it means it is expired.
  return isBeforeDate(selectedSprint.endsAt, current);
}
