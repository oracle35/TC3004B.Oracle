import { useEffect, useState } from "react";
import { Button, CircularProgress } from "@mui/material";
import "./App.css";
import { getTasks, updateTask, deleteTask } from "./api/task";
import { getUsers } from "./api/user";
import { Task } from "./models/Task";
import { User } from "./models/User";
import ErrorMessage from "./components/Error/Error";
import TaskTable from "./components/TaskTable";
import MainTitle from "./components/MainTitle";
import AddModal from "./components/AddModal/AddModal";
import KpiModal from "./components/KpiModal/KpiModal";

function App() {
  const [loading, setLoading] = useState<boolean>(false);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string>("");
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [showStats, setShowStats] = useState<boolean>(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData] = await Promise.all([
          getTasks(),
          getUsers(),
        ]);
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
        setUsers(usersData);
        console.log(`Users: ${usersData}`);
      } catch (error) {
        console.error(error);
        setError("Error fetching data");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const reloadTasks = async () => {
    if (!loading) {
      setLoading(true);
      try {
        const tasksData = await getTasks();
        setTasks(
          tasksData.sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
        );
      } catch (error) {
        console.error(error);
        setError("Error reloading tasks");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleStateChange = async (task: Task, newState: string, hrsReales: number) => {
    try {
      const updatedTask = { ...task, state: newState, hoursReal: hrsReales };
      await updateTask(task.id_Task, updatedTask);
      setTasks((prevTasks) =>
        prevTasks
          .map((t) => (t.id_Task === task.id_Task ? updatedTask : t))
          .sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
      );
    } catch (error) {
      console.error(error);
      setError("Error updating task state");
    }
  }

  const handleEdit = async (task: Task) => {
    // This will be implemented when we add the edit modal
    console.log("Edit task:", task);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      // Update the local state instead of reloading
      setTasks((prevTasks) =>
        prevTasks
          .filter((t) => t.id_Task !== id)
          .sort((a: Task, b: Task) =>
            a.description.localeCompare(b.description)
          )
      );
    } catch (error) {
      console.error(error);
      setError("Error deleting task");
    }
  };

  const handleOpen = () => {
    setShowAddModal(true);
  };

  const handleClose = () => {
    setShowAddModal(false);
  };

  const handleAddTask = async () => {
    try {
      const tasksData = await getTasks();
      setTasks(
        tasksData.sort((a: Task, b: Task) =>
          a.description.localeCompare(b.description)
        )
      );
    } catch (error) {
      console.error(error);
      setError("Error adding task");
    }
  };

  const handleShowStats = () => {
    setShowStats(true);
  };

  const handleCloseStats = () => {
    setShowStats(false);
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
              <AddModal
                open={showAddModal}
                onClose={handleClose}
                reloadTable={reloadTasks}
                setLoading={setLoading}
                sprintId={1}
                addTask={handleAddTask}
              />
              {/** Constant for now. */}
                <Button
                  onClick={handleShowStats}
                  variant="outlined"
                  style={{ margin: "10px", padding: "10px" }}
                >
                  Show Stats
                </Button>
                <Button
                  onClick={handleOpen}
                  variant="outlined"
                  style={{ margin: "10px", padding: "10px" }}
                >
                  Add Task
                </Button>

              <TaskTable
                tasks={tasks}
                users={users}
                handleDelete={handleDelete}
                handleEdit={handleEdit}
                handleStateChange={handleStateChange}
              />
            </div>
          </div>
        )}

        <KpiModal
          tasks={tasks}
          open={showStats}
          onClose={handleCloseStats}
          users={users}
        />
      </div>
    </div>
  );
}

export default App;
