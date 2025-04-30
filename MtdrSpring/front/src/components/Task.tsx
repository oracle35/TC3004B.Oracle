import { ToDoElement } from "../models/ToDoElement";

export const Task = ({
  description,
  delivery_ts,
  creation_ts,
  done,
}: ToDoElement) => {
  return (
    <tr className="p-4 border rounded shadow-md bg-white">
      <td className="text-xl font-bold mb-2">{description}</td>
      <td className="text-gray-600">{creation_ts.toDateString()}</td>
      {delivery_ts && (
        <td className="text-gray-600">{delivery_ts.toDateString()}</td>
      )}
      <td className={`mt-2 ${done ? "text-green-500" : "text-red-500"}`}>
        {done ? "Done" : "Not done"}
      </td>
    </tr>
  );
};
