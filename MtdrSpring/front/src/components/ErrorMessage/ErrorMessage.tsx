const ErrorMessage = ({ error }: { error: string }) => {
  return (
    <div
      className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
      role="alert"
    >
      <p className="font-bold">Error</p>
      <p>{error}</p>
    </div>
  );
};
export default ErrorMessage;
