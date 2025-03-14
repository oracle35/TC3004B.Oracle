/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * This is the application main React component. We're using "function"
 * components in this application. No "class" components should be used for
 * consistency.
 * @author  jean.de.lavarene@oracle.com
 */
import React, { useState, useEffect } from "react";
import NewItem from "./NewItem";
import API_LIST from "./API";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  Button,
  TableBody,
  CircularProgress,
  Box,
  Typography,
} from "@mui/material";
import Moment from "react-moment";
import Modal from "@mui/material/Modal";

/* In this application we're using Function Components with the State Hooks
 * to manage the states. See the doc: https://reactjs.org/docs/hooks-state.html
 * This App component represents the entire app. It renders a NewItem component
 * and two tables: one that lists the todo items that are to be done and another
 * one with the items that are already done.
 */

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

function App() {
  // isLoading is true while waiting for the backend to return the list
  // of items. We use this state to display a spinning circle:
  const [isLoading, setLoading] = useState(false);
  // Similar to isLoading, isInserting is true while waiting for the backend
  // to insert a new item:
  const [isInserting, setInserting] = useState(false);
  // The list of todo items is stored in this state. It includes the "done"
  // "not-done" items:
  const [items, setItems] = useState([]);
  // In case of an error during the API call:
  const [error, setError] = useState();
  const [isModal, setIsModal] = useState(false);

  function deleteItem(deleteId) {
    // console.log("deleteItem("+deleteId+")")
    fetch(API_LIST + "/" + deleteId, {
      method: "DELETE",
    })
      .then((response) => {
        // console.log("response=");
        // console.log(response);
        if (response.ok) {
          // console.log("deleteItem FETCH call is ok");
          return response;
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        (result) => {
          const remainingItems = items.filter((item) => item.id !== deleteId);
          setItems(remainingItems);
        },
        (error) => {
          setError(error);
        }
      );
  }
  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
      (result) => {
        reloadOneIteam(id);
      },
      (error) => {
        setError(error);
      }
    );
  }
  function reloadOneIteam(id) {
    fetch(API_LIST + "/" + id)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        (result) => {
          const items2 = items.map((x) =>
            x.id === id
              ? {
                  ...x,
                  description: result.description,
                  done: result.done,
                }
              : x
          );
          setItems(items2);
        },
        (error) => {
          setError(error);
        }
      );
  }
  function modifyItem(id, description, done) {
    // console.log("deleteItem("+deleteId+")")
    var data = { description: description, done: done };
    return fetch(API_LIST + "/" + id, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }).then((response) => {
      // console.log("response=");
      // console.log(response);
      if (response.ok) {
        // console.log("deleteItem FETCH call is ok");
        return response;
      } else {
        throw new Error("Something went wrong ...");
      }
    });
  }
  /*
    To simulate slow network, call sleep before making API calls.
    const sleep = (milliseconds) => {
      return new Promise(resolve => setTimeout(resolve, milliseconds))
    }
    */
  useEffect(
    () => {
      setLoading(true);
      // sleep(5000).then(() => {
      fetch(API_LIST)
        .then((response) => {
          if (response.ok) {
            console.log(
              `response.ok=${response.ok} \t response data = ${JSON.stringify(response)}`
            );
            return response.json();
          } else {
            throw new Error("Something went wrong ...");
          }
        })
        .then(
          (result) => {
            setLoading(false);
            console.log("result=" + JSON.stringify(result));
            setItems(result);
          },
          (error) => {
            setLoading(false);
            setError(error);
          }
        );

      //})
    },
    // https://en.reactjs.org/docs/faq-ajax.html
    [] // empty deps array [] means
    // this useEffect will run once
    // similar to componentDidMount()
  );
  function addItem(text, deliveryDate) {
    console.log("addItem(" + text + ")");
    setInserting(true);
    var data = {};
    console.log(data);
    data.description = text;

    data.delivery_ts = deliveryDate;
    data.creation_ts = new Date();
    fetch(API_LIST, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
      .then((response) => {
        // This API doens't return a JSON document
        console.log(response);
        console.log();
        console.log(response.headers.location);
        // return response.json();
        if (response.ok) {
          return response;
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        (result) => {
          var id = result.headers.get("location");
          var newItem = {
            id: id,
            description: text,
            creation_ts: data.creation_ts,
            delivery_ts: deliveryDate,
            done: false,
          };
          setItems([newItem, ...items]);
          setInserting(false);
        },
        (error) => {
          setInserting(false);
          setError(error);
        }
      );
  }
  return (
    <div className="App">
      <h1>MY TODO LIST</h1>
      <NewItem addItem={addItem} isInserting={isInserting} />
      <Button onClick={() => setIsModal(true)}>Open Modal</Button>

      {error && <p>Error: {error.message}</p>}
      {isLoading && <CircularProgress />}
      {!isLoading && (
        <div id="maincontent">
          {isModal && (
            <Modal open={isModal} onClose={() => setIsModal(false)}>
              <Box sx={style}>
                <form
                  onSubmit={(e) => {
                    e.preventDefault();
                    const formData = new FormData(e.target);
                    const description = formData.get("description");
                    const deliveryDate = new Date(
                      formData.get("deliveryDate") + "T00:00:00" // Fix date format
                    );
                    addItem(description, deliveryDate);
                    setIsModal(false);
                  }}
                >
                  <div>
                    <label htmlFor="description">Description:</label>
                    <input
                      type="text"
                      id="description"
                      name="description"
                      required
                    />
                  </div>
                  <div>
                    <label htmlFor="deliveryDate">Delivery Date:</label>
                    <input
                      type="date"
                      id="deliveryDate"
                      name="deliveryDate"
                      required
                    />
                  </div>
                  <Button type="submit" variant="contained">
                    Add Item
                  </Button>
                </form>
              </Box>
            </Modal>
          )}

          <table id="itemlistNotDone" className="itemlist">
            <TableBody>
              {items.map(
                (item) =>
                  !item.done && (
                    <tr key={item.id}>
                      <td className="description">{item.description}</td>
                      {/*<td>{JSON.stringify(item, null, 2) }</td>*/}
                      <td
                        className="date"
                        style={{
                          display: "flex",
                          flexDirection: "column",
                          justifyContent: "center",
                          alignItems: "center",
                          textAlign: "center",
                        }}
                      >
                        <Typography>
                          <Moment format="MMM Do hh:mm:ss">
                            {item.creation_ts}
                          </Moment>{" "}
                        </Typography>

                        <Typography>
                          {(() => {
                            const diffDays = Math.ceil(
                              (new Date(item.delivery_ts) - new Date()) /
                                (1000 * 60 * 60 * 24)
                            );
                            return (
                              diffDays +
                              (diffDays === 1 ? " Day Left" : " Days Left")
                            );
                          })()}
                        </Typography>
                      </td>
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
                          size="small"
                        >
                          Done
                        </Button>
                      </td>
                    </tr>
                  )
              )}
            </TableBody>
          </table>
          <h2 id="donelist">Done items</h2>
          <table id="itemlistDone" className="itemlist">
            <TableBody>
              {items.map(
                (item) =>
                  item.done && (
                    <tr key={item.id}>
                      <td className="description">{item.description}</td>
                      <td className="date">
                        <Moment format="MMM Do hh:mm:ss">
                          {item.creation_ts}
                        </Moment>
                      </td>
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
                          size="small"
                        >
                          Undo
                        </Button>
                      </td>
                      <td>
                        <Button
                          startIcon={<DeleteIcon />}
                          variant="contained"
                          className="DeleteButton"
                          onClick={() => deleteItem(item.id)}
                          size="small"
                        >
                          Delete
                        </Button>
                      </td>
                    </tr>
                  )
              )}
            </TableBody>
          </table>
        </div>
      )}
    </div>
  );
}
export default App;
