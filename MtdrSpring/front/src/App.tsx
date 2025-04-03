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

function App() {
  const [loading, setLoading] = useState<boolean>(false);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [error, setError] = useState<string>("");
  const [showAddModal, setShowAddModal] = useState<boolean>(false);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [tasksData, usersData] = await Promise.all([
          getTasks(),
          getUsers()
        ]);
        setTasks(tasksData);
        setUsers(usersData);
        console.log(`Users: ${usersData}`)
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
        setTasks(tasksData);
      } catch (error) {
        console.error(error);
        setError("Error reloading tasks");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleStateChange = async (task: Task, newState: string) => {
    if (!loading) {
      setLoading(true);
      try {
        const updatedTask = { ...task, state: newState };
        await updateTask(task.id_Task, updatedTask);
        await reloadTasks();
      } catch (error) {
        console.error(error);
        setError("Error updating task state");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleEdit = async (task: Task) => {
    // This will be implemented when we add the edit modal
    console.log("Edit task:", task);
  };

  const handleDelete = async (id: number) => {
    if (!loading) {
      setLoading(true);
      try {
        await deleteTask(id);
        await reloadTasks();
      } catch (error) {
        console.error(error);
        setError("Error deleting task");
      } finally {
        setLoading(false);
      }
    }
  };

  const handleOpen = () => {
    setShowAddModal(true);
  };

  const handleClose = () => {
    setShowAddModal(false);
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
                sprintId = {1} 
                
              />
              {/** Constant for now. */}
              <Button onClick={handleOpen}>Add Task</Button>

              <h3>Tasks</h3>
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
      </div>
    </div>
  );
}

export default App;
