//
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
    if (sprint.startsAt && sprint.endsAt) {
      const current = new Date();
      if (isBetweenDates(sprint.startsAt, sprint.endsAt, current)) {
        // ?? Early return c:
        return sprint;
      }
    }
  }

  console.log("No Current Sprint found.");
  return;
}
