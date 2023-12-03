package com.example.todoapp;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    public static final String KEY_EXTRA_TASK_ID = "tasklistfragment.task_id";

    public TaskListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_task) {
            Task task = new Task();
            TaskStorage.getInstance().addTask(task);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(TaskListFragment.KEY_EXTRA_TASK_ID, task.getId());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.show_subtitle) {
            updateSubtitle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Add menu
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);
        recyclerView = view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        TaskStorage taskStorage = TaskStorage.getInstance();
        List<Task> tasks = taskStorage.getTasks();

        if (adapter == null) {
            adapter = new TaskAdapter(tasks);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView, dateTextView;
        private Task task;
        private ImageView iconImageView;
        private CheckBox checkbox;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_task, parent, false));
            itemView.setOnClickListener(this);

            nameTextView = itemView.findViewById(R.id.task_item_name);
            dateTextView = itemView.findViewById(R.id.task_item_date);
            iconImageView = itemView.findViewById(R.id.task_item_category);
            checkbox = itemView.findViewById(R.id.task_item_checkbox);
        }

        public void bind(Task task) {
            this.task = task;

            nameTextView.setText(task.getName());
            dateTextView.setText(task.getDate().toString());

            // If task is done, set strikethrough
            if (task.isDone()) {
                nameTextView.setPaintFlags(nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                nameTextView.setPaintFlags(nameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Validate that the task name can fit on one line
            nameTextView.setSingleLine(true);
            // If not, shorten it and add three dots at the end
            nameTextView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            // Test it by uncommenting the line below
            // nameTextView.setText("This is a very long task name that should be shortened");

            if (task.getCategory().equals(Category.HOME)) {
                iconImageView.setImageResource(R.drawable.ic_house);
            } else {
                iconImageView.setImageResource(R.drawable.ic_university);
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(KEY_EXTRA_TASK_ID, task.getId());
            startActivity(intent);
        }

        public CheckBox getCheckbox() {
            return checkbox;
        }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {
        private List<Task> tasks;

        public TaskAdapter(List<Task> tasks) {
            this.tasks = tasks;
        }

        @NonNull
        @Override
        public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TaskHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
            Task task = tasks.get(position);
            holder.bind(task);

            CheckBox checkBox = holder.getCheckbox();
            checkBox.setChecked(tasks.get(position).isDone());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tasks.get(holder.getBindingAdapterPosition()).setDone(isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }
    }

    public void updateSubtitle() {
        TaskStorage taskStorage = TaskStorage.getInstance();
        List<Task> tasks = taskStorage.getTasks();
        int todoTasksCount = 0;
        for (Task task : tasks) {
            if (!task.isDone()) {
                todoTasksCount++;
            }
        }
        String subtitle = getString(R.string.subtitle_format, todoTasksCount);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
    }
}
