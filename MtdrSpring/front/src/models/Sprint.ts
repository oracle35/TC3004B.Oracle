/**
 * Sprint model
 * This model represents a sprint in the system.
 * It contains information about the sprint such as its ID, name, description, start and end dates,
 * and the project ID it belongs to.
 */

export interface Sprint { 
    id_sprint: number;
    name: string;
    description: string;
    startsAt: Date;
    endsAt: Date;
    id_project: number;
}