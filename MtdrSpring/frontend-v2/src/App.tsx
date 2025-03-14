import { useEffect, useState } from "react";
import { Button, CircularProgress, TableBody } from "@mui/material";
import "./App.css";
import { getItems, modifyItem, reloadOneItem } from "./api/todo";
import { ToDoElement } from "./models/ToDoElement";

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

  const toggleDone = (
    event: React.MouseEvent<HTMLButtonElement>,
    id: number,
    description: string,
    done: boolean
  ) => {
    event.preventDefault();
    modifyItem(id, description, done)
      .then(() => {
        // Result of the promise
        reloadOneItem(id);
      })
      .catch((error) => {
        console.error(error);
        setError("Error while updating item");
      });
  };

  return (
    <>
      <div>
        <h1>Oracle Redesign</h1>
        {error && <div>{error}</div>}
        {loading && <CircularProgress />}
        {!loading && (
          <div>
            <div>
              <table>
                <TableBody>
                  {items.map(
                    (item) =>
                      !item.done && (
                        <tr key={item.id}>
                          <td className="description">{item.description}</td>

                          {/**
                           * TODO: Replace this with actual date model. Since <Moments> doesn't seem to be working properly... */}
                          <td className="date"></td>
                          <td>
                            <Button
                              variant="contained"
                              className="DoneButton"
                              onClick={(event) =>
                                toggleDone(
                                  event,
                                  item.id,
                                  item.description,
                                  !item.done
                                )
                              }
                            >
                              Done
                            </Button>
                          </td>
                        </tr>
                      )
                  )}
                </TableBody>
              </table>
            </div>
            <div>
              <h3>Done items</h3>
              <table>
                <TableBody>
                  {items.map(
                    (item) =>
                      item.done && (
                        <div key={item.id}>
                          <p>{item.description}</p>
                          <input type="checkbox" checked={item.done} />
                        </div>
                      )
                  )}
                </TableBody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default App;
