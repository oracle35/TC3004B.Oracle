import { useEffect, useState } from "react";
import { CircularProgress } from "@mui/material";
import "./App.css";
import { API_LIST, getItems, modifyItem } from "./api/todo";
import { ToDoElement } from "./models/ToDoElement";
import ErrorMessage from "./components/Error/Error";
import TaskTable from "./components/TaskTable";
import MainTitle from "./components/MainTitle";

function App() {
  const [loading, setLoading] = useState<boolean>(false);
  const [items, setItems] = useState<ToDoElement[]>([]);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    setLoading(true);
    getItems()
      .then((data) => {
        setItems(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error(error);
        setLoading(false);
      });
  }, []);

  const reloadItems = (id: number) => {
    if (!loading) {
      setLoading(true);
    }
    fetch(API_LIST + "/" + id)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Error reloading item with id " + id);
        }
      })
      .then((data) => {
        const newItems = items.map((item) =>
          item.id === id
            ? {
                ...item,
                description: data.description,
                done: data.done,
              }
            : item
        );

        setItems(newItems);
        setLoading(false);
      })
      .catch((error) => {
        console.error(error);
        setLoading(false);
      });
  };

  const toggleDone = (
    event: React.MouseEvent<HTMLButtonElement>,
    id: number,
    description: string,
    done: boolean
  ) => {
    event.preventDefault();
    if (!loading) {
      setLoading(true);
    }
    modifyItem(id, description, done)
      .then(() => {
        reloadItems(id);
      })
      .catch((error) => {
        console.error(error);
        setError("Error while updating item");
      });
  };

  return (
    <div className="flex flex-col">
      <div>
        <MainTitle title="Oracle Task Management System" />
        {error && <ErrorMessage error={error} />}

        {loading && <CircularProgress />}
        {!loading && (
          <div>
            <div>
              <h3>Pending Items</h3>
              <TaskTable tasks={items} done={false} toggleDone={toggleDone} />
            </div>
            <div>
              <h3>Done Items</h3>
              <TaskTable tasks={items} done={true} toggleDone={toggleDone} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
