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
import API_LIST from "./API";
import DeleteIcon from "@mui/icons-material/Delete";
import {
  Button,
  TableBody,
  CircularProgress,
  Box,
  Typography,
  Backdrop,
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
  const [isLoading, setLoading] = useState(false);
  const [, setInserting] = useState(false);
  const [items, setItems] = useState([]);
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
    <div
      className="App"
      style={{ padding: "20px", fontFamily: "Arial, sans-serif" }}
    >
      <Typography variant="h3" gutterBottom>
        Tasks Portal
      </Typography>
      <Button
        variant="contained"
        color="primary"
        onClick={() => setIsModal(true)}
      >
        New Task
      </Button>

      {error && <Typography color="error">Error: {error.message}</Typography>}
      {isLoading && <CircularProgress />}
      {!isLoading && (
        <div id="maincontent">
          {isModal && (
            <Modal
              open={isModal}
              onClose={() => setIsModal(false)}
              closeAfterTransition
              slots={{ backdrop: Backdrop }}
              slotProps={{
                backdrop: {
                  timeout: 500,
                },
              }}
            >
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
                  <div style={{ marginBottom: "15px" }}>
                    <label htmlFor="description">Description:</label>
                    <input
                      type="text"
                      id="description"
                      name="description"
                      required
                      style={{
                        width: "100%",
                        padding: "8px",
                        marginTop: "5px",
                      }}
                    />
                  </div>
                  <div style={{ marginBottom: "15px" }}>
                    <label htmlFor="deliveryDate">Delivery Date:</label>
                    <input
                      type="date"
                      id="deliveryDate"
                      name="deliveryDate"
                      required
                      style={{
                        width: "100%",
                        padding: "8px",
                        marginTop: "5px",
                      }}
                    />
                  </div>
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    fullWidth
                  >
                    Add Item
                  </Button>
                </form>
              </Box>
            </Modal>
          )}

          <table
            id="itemlistNotDone"
            className="itemlist"
            style={{ width: "100%", marginTop: "20px" }}
          >
            <TableBody>
              {items.map(
                (item) =>
                  !item.done && (
                    <tr key={item.id}>
                      <td
                        className="description"
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        {item.description}
                      </td>
                      <td
                        className="date"
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                          textAlign: "center",
                        }}
                      >
                        <Typography>
                          <Moment format="MMM Do hh:mm:ss">
                            {item.creation_ts}
                          </Moment>
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
                      <td
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        <Button
                          variant="contained"
                          color="secondary"
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
          <Typography variant="h5" gutterBottom style={{ marginTop: "20px" }}>
            Done items
          </Typography>
          <table
            id="itemlistDone"
            className="itemlist"
            style={{ width: "100%", marginTop: "10px" }}
          >
            <TableBody>
              {items.map(
                (item) =>
                  item.done && (
                    <tr key={item.id}>
                      <td
                        className="description"
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        {item.description}
                      </td>
                      <td
                        className="date"
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        <Moment format="MMM Do hh:mm:ss">
                          {item.creation_ts}
                        </Moment>
                      </td>
                      <td
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        <Button
                          variant="contained"
                          color="secondary"
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
                      <td
                        style={{
                          padding: "10px",
                          borderBottom: "1px solid #ccc",
                        }}
                      >
                        <Button
                          startIcon={<DeleteIcon />}
                          variant="contained"
                          color="error"
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
