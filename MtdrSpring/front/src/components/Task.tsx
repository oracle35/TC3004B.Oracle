interface TaskProp {
  description: string;
  createdAt: Date;
  deliveredAt?: Date;
  done: boolean;
}

export const Task: React.FC<TaskProp> = ({
  description,
  createdAt,
  deliveredAt,
  done,
}) => {
  return (
    <div className="p-4 border rounded shadow-md bg-white">
      <h1 className="text-xl font-bold mb-2">{description}</h1>
      <p className="text-gray-600">{createdAt.toDateString()}</p>
      {deliveredAt && (
        <p className="text-gray-600">{deliveredAt.toDateString()}</p>
      )}
      <p className={`mt-2 ${done ? "text-green-500" : "text-red-500"}`}>
        {done ? "Done" : "Not done"}
      </p>
    </div>
  );
};
